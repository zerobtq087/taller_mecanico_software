package com.taller.security.repository;

import com.taller.security.model.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByCustomerId(Long customerId);

    Optional<Vehicle> findByIdAndCustomerId(Long id, Long customerId);

    boolean existsByVin(String vin);

    boolean existsByPlate(String plate);

    Optional<Vehicle> findByVin(String vin);

    Optional<Vehicle> findByPlate(String plate);
}
