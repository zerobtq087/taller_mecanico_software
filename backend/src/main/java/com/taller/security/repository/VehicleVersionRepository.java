package com.taller.security.repository;

import com.taller.security.model.VehicleVersion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleVersionRepository extends JpaRepository<VehicleVersion, Long> {
    @Query("""
            select vv from VehicleVersion vv
            where lower(vv.model.make.name) = lower(:make)
              and lower(vv.model.name) = lower(:model)
              and (:query is null or lower(vv.name) like lower(concat('%', :query, '%')))
            order by vv.name asc
            """)
    List<VehicleVersion> searchByMakeAndModel(
            @Param("make") String make,
            @Param("model") String model,
            @Param("query") String query
    );
}
