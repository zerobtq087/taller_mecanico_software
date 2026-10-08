package com.taller.security.repository;

import com.taller.security.model.VehicleModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    @Query("""
            select vm from VehicleModel vm
            where lower(vm.make.name) = lower(:make)
              and (:query is null or lower(vm.name) like lower(concat('%', :query, '%')))
            order by vm.name asc
            """)
    List<VehicleModel> searchByMake(@Param("make") String make, @Param("query") String query);
}
