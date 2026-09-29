package com.taller.security.service;

import com.taller.security.dto.CustomerDtos.CustomerRequest;
import com.taller.security.dto.CustomerDtos.CustomerResponse;
import com.taller.security.model.Customer;
import com.taller.security.model.User;
import com.taller.security.repository.CustomerRepository;
import com.taller.security.repository.UserRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private static final Pattern IMAGE_DATA_URL = Pattern.compile("^data:image/(png|jpeg|jpg|webp|gif);base64,.+");

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Registra un cliente validando duplicados por email y telefono personal antes de persistir.
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, String createdByEmail) {
        String email = normalizeEmail(request.email());
        String personalPhone = normalizePhone(request.personalPhone());
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con ese email");
        }
        if (customerRepository.existsByPersonalPhone(personalPhone)) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con ese telefono personal");
        }

        User createdBy = userRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));

        Customer customer = new Customer();
        customer.setFullName(request.fullName().trim());
        customer.setAlternateContactName(request.alternateContactName().trim());
        customer.setAge(request.age());
        customer.setBirthDate(request.birthDate());
        customer.setPersonalPhone(personalPhone);
        customer.setWorkPhone(normalizePhone(request.workPhone()));
        customer.setEmail(email);
        customer.setWorkEmail(normalizeOptionalEmail(request.workEmail()));
        customer.setPhotoDataUrl(validatePhotoDataUrl(request.photoDataUrl()));
        customer.setStreet(request.street().trim());
        customer.setNeighborhood(request.neighborhood().trim());
        customer.setMunicipality(request.municipality().trim());
        customer.setState(request.state().trim());
        customer.setPostalCode(request.postalCode().trim());
        customer.setWorkshopId(request.workshopId());
        customer.setCreatedBy(createdBy);
        return toResponse(customerRepository.save(customer));
    }

    /**
     * Lista los clientes disponibles para modulos operativos y futuras asociaciones con talleres.
     */
    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Convierte la entidad JPA a un DTO seguro para la vista.
     */
    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getAlternateContactName(),
                customer.getAge(),
                customer.getBirthDate(),
                customer.getPersonalPhone(),
                customer.getWorkPhone(),
                customer.getEmail(),
                customer.getWorkEmail(),
                customer.getPhotoDataUrl(),
                customer.getStreet(),
                customer.getNeighborhood(),
                customer.getMunicipality(),
                customer.getState(),
                customer.getPostalCode(),
                customer.getWorkshopId(),
                customer.getCreatedBy().getId(),
                customer.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeOptionalEmail(String email) {
        return email == null || email.isBlank() ? null : normalizeEmail(email);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizePhone(String phone) {
        return phone.trim().replaceAll("\\s+", " ");
    }

    private String validatePhotoDataUrl(String value) {
        String photoDataUrl = normalizeOptional(value);
        if (photoDataUrl != null && !IMAGE_DATA_URL.matcher(photoDataUrl).matches()) {
            throw new IllegalArgumentException("La foto debe ser una imagen valida");
        }
        return photoDataUrl;
    }
}
