package com.taller.security.repository;

import com.taller.security.model.VehicleMake;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleMakeRepository extends JpaRepository<VehicleMake, Long> {
    @Query("""
            select m from VehicleMake m
            where lower(m.name) like lower(concat('%', :query, '%'))
            order by m.name asc
            """)
    List<VehicleMake> search(@Param("query") String query);
}
