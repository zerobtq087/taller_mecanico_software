package com.taller.security.repository;

import com.taller.security.model.Customer;
import com.taller.security.model.CustomerWorkshop;
import com.taller.security.model.Workshop;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerWorkshopRepository extends JpaRepository<CustomerWorkshop, Long> {
    Optional<CustomerWorkshop> findByCustomerAndWorkshop(Customer customer, Workshop workshop);

    List<CustomerWorkshop> findByCustomerIdOrderByLastVisitAtDesc(Long customerId);

    void deleteByCustomerIdAndWorkshopIdNotIn(Long customerId, List<Long> workshopIds);
}
