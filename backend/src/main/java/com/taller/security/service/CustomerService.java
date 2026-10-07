package com.taller.security.service;

import com.taller.security.dto.CustomerDtos.CustomerRequest;
import com.taller.security.dto.CustomerDtos.CustomerResponse;
import com.taller.security.dto.CustomerDtos.CustomerUpdateRequest;
import com.taller.security.dto.CustomerDtos.WorkshopVisitResponse;
import com.taller.security.model.Customer;
import com.taller.security.model.CustomerStatus;
import com.taller.security.model.CustomerWorkshop;
import com.taller.security.model.User;
import com.taller.security.model.Workshop;
import com.taller.security.repository.CustomerRepository;
import com.taller.security.repository.CustomerWorkshopRepository;
import com.taller.security.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerWorkshopRepository customerWorkshopRepository;
    private final UserRepository userRepository;
    private final WorkshopService workshopService;
    private final FileStorageService fileStorageService;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerWorkshopRepository customerWorkshopRepository,
            UserRepository userRepository,
            WorkshopService workshopService,
            FileStorageService fileStorageService
    ) {
        this.customerRepository = customerRepository;
        this.customerWorkshopRepository = customerWorkshopRepository;
        this.userRepository = userRepository;
        this.workshopService = workshopService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Registra un cliente nuevo o reutiliza uno existente y deja trazabilidad del taller visitado.
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, String createdByEmail, org.springframework.web.multipart.MultipartFile photo) {
        User createdBy = getAuthenticatedUser(createdByEmail);
        Workshop workshop = workshopService.getRequiredWorkshop(request.currentWorkshopId());
        NormalizedCustomer normalized = normalize(request);
        String photoPath = fileStorageService.saveRequiredCustomerPhoto(photo);

        Customer customer = customerRepository
                .findExistingIdentity(normalized.curp(), normalized.rfc(), normalized.email())
                .orElseGet(Customer::new);
        boolean existingCustomer = customer.getId() != null;

        if (!existingCustomer) {
            customer.setCreatedBy(createdBy);
            customer.setStatus(CustomerStatus.ACTIVO);
            customer.setPhotoPath(photoPath);
        } else if (customer.getPhotoPath() == null || customer.getPhotoPath().isBlank()) {
            customer.setPhotoPath(photoPath);
        }
        applyCustomerData(customer, normalized);
        Customer saved = customerRepository.save(customer);
        registerVisit(saved, workshop, createdBy);
        return toResponse(saved, existingCustomer);
    }

    /**
     * Lista clientes con paginacion de servidor, filtro opcional por taller y orden dinamico.
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(Long workshopId, Pageable pageable) {
        return customerRepository.findForAdministration(workshopId, pageable)
                .map(customer -> toResponse(customer, false));
    }

    /**
     * Edita los datos maestros del cliente y sincroniza los talleres asociados.
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request, String updatedByEmail, org.springframework.web.multipart.MultipartFile photo) {
        User updatedBy = getAuthenticatedUser(updatedByEmail);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        NormalizedCustomer normalized = normalize(request);
        customerRepository
                .findDuplicatedIdentityForUpdate(id, normalized.curp(), normalized.rfc(), normalized.email())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe otro cliente con CURP, RFC o email capturado");
                });

        applyCustomerData(customer, normalized);
        String photoPath = fileStorageService.saveOptionalCustomerPhoto(photo);
        if (photoPath != null) {
            customer.setPhotoPath(photoPath);
        }
        syncWorkshops(customer, request.workshopIds(), updatedBy);
        return toResponse(customer, false);
    }

    /**
     * Aplica borrado logico global para todos los talleres asociados al cliente.
     */
    @Transactional
    public CustomerResponse suspendCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        customer.setStatus(CustomerStatus.SUSPENDIDO);
        return toResponse(customer, false);
    }

    /**
     * Convierte la entidad JPA en DTO de salida incluyendo historial por taller.
     */
    public CustomerResponse toResponse(Customer customer, boolean existingCustomer) {
        List<WorkshopVisitResponse> visits = customerWorkshopRepository
                .findByCustomerIdOrderByLastVisitAtDesc(customer.getId())
                .stream()
                .map(this::toVisitResponse)
                .toList();
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getSecondLastName(),
                buildFullName(customer),
                customer.getAlternateContactName(),
                calculateAge(customer.getBirthDate()),
                customer.getBirthDate(),
                customer.getCurp(),
                customer.getRfc(),
                customer.getContactPhone(),
                customer.getWorkPhone(),
                customer.getEmail(),
                customer.getWorkEmail(),
                customer.getStreet(),
                customer.getNeighborhood(),
                customer.getMunicipality(),
                customer.getState(),
                customer.getPostalCode(),
                customer.getPhotoPath(),
                customer.getStatus(),
                visits,
                customer.getCreatedBy().getId(),
                customer.getCreatedAt(),
                existingCustomer
        );
    }

    private void applyCustomerData(Customer customer, NormalizedCustomer normalized) {
        customer.setFirstName(normalized.firstName());
        customer.setLastName(normalized.lastName());
        customer.setSecondLastName(normalized.secondLastName());
        customer.setAlternateContactName(normalized.alternateContactName());
        customer.setBirthDate(normalized.birthDate());
        customer.setCurp(normalized.curp());
        customer.setRfc(normalized.rfc());
        customer.setContactPhone(normalized.contactPhone());
        customer.setWorkPhone(normalized.workPhone());
        customer.setEmail(normalized.email());
        customer.setWorkEmail(normalized.workEmail());
        customer.setStreet(normalized.street());
        customer.setNeighborhood(normalized.neighborhood());
        customer.setMunicipality(normalized.municipality());
        customer.setState(normalized.state());
        customer.setPostalCode(normalized.postalCode());
    }

    private void registerVisit(Customer customer, Workshop workshop, User user) {
        Instant now = Instant.now();
        CustomerWorkshop relation = customerWorkshopRepository.findByCustomerAndWorkshop(customer, workshop)
                .orElseGet(CustomerWorkshop::new);
        relation.setCustomer(customer);
        relation.setWorkshop(workshop);
        relation.setRegisteredBy(user);
        relation.setLastVisitAt(now);
        if (relation.getFirstVisitAt() == null) {
            relation.setFirstVisitAt(now);
        }
        customerWorkshopRepository.save(relation);
    }

    private void syncWorkshops(Customer customer, List<Long> workshopIds, User user) {
        Set<Long> uniqueWorkshopIds = new LinkedHashSet<>(workshopIds);
        uniqueWorkshopIds.forEach(workshopId -> registerVisit(customer, workshopService.getRequiredWorkshop(workshopId), user));
        customerWorkshopRepository.deleteByCustomerIdAndWorkshopIdNotIn(customer.getId(), List.copyOf(uniqueWorkshopIds));
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
    }

    private WorkshopVisitResponse toVisitResponse(CustomerWorkshop relation) {
        return new WorkshopVisitResponse(
                relation.getWorkshop().getId(),
                relation.getWorkshop().getName(),
                relation.getFirstVisitAt(),
                relation.getLastVisitAt(),
                relation.getRegisteredBy().getId()
        );
    }

    private String buildFullName(Customer customer) {
        return String.join(" ", customer.getFirstName(), customer.getLastName(), customer.getSecondLastName());
    }

    private Integer calculateAge(LocalDate birthDate) {
        return birthDate == null ? null : Period.between(birthDate, LocalDate.now()).getYears();
    }

    private NormalizedCustomer normalize(CustomerRequest request) {
        if (request.birthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
        return new NormalizedCustomer(
                normalizeText(request.firstName()),
                normalizeText(request.lastName()),
                normalizeText(request.secondLastName()),
                normalizeText(request.alternateContactName()),
                request.birthDate(),
                normalizeUpper(request.curp()),
                normalizeUpper(request.rfc()),
                normalizePhone(request.contactPhone()),
                normalizePhone(request.workPhone()),
                normalizeEmail(request.email()),
                normalizeOptionalEmail(request.workEmail()),
                normalizeText(request.street()),
                normalizeText(request.neighborhood()),
                normalizeText(request.municipality()),
                normalizeText(request.state()),
                request.postalCode().trim()
        );
    }

    private NormalizedCustomer normalize(CustomerUpdateRequest request) {
        if (request.birthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
        return new NormalizedCustomer(
                normalizeText(request.firstName()),
                normalizeText(request.lastName()),
                normalizeText(request.secondLastName()),
                normalizeText(request.alternateContactName()),
                request.birthDate(),
                normalizeUpper(request.curp()),
                normalizeUpper(request.rfc()),
                normalizePhone(request.contactPhone()),
                normalizePhone(request.workPhone()),
                normalizeEmail(request.email()),
                normalizeOptionalEmail(request.workEmail()),
                normalizeText(request.street()),
                normalizeText(request.neighborhood()),
                normalizeText(request.municipality()),
                normalizeText(request.state()),
                request.postalCode().trim()
        );
    }

    private String normalizeText(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeOptionalEmail(String email) {
        return email == null || email.isBlank() ? null : normalizeEmail(email);
    }

    private String normalizePhone(String phone) {
        return phone.trim().replaceAll("\\D", "");
    }

    private String normalizeUpper(String value) {
        return value.trim().replaceAll("\\s+", "").toUpperCase();
    }

    private record NormalizedCustomer(
            String firstName,
            String lastName,
            String secondLastName,
            String alternateContactName,
            LocalDate birthDate,
            String curp,
            String rfc,
            String contactPhone,
            String workPhone,
            String email,
            String workEmail,
            String street,
            String neighborhood,
            String municipality,
            String state,
            String postalCode
    ) {
    }
}
