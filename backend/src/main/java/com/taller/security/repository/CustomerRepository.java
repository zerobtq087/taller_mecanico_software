package com.taller.security.repository;

import com.taller.security.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);

    boolean existsByPersonalPhone(String personalPhone);

    Optional<Customer> findByEmail(String email);
}
