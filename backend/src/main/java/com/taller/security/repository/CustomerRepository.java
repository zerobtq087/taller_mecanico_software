package com.taller.security.repository;

import com.taller.security.model.Customer;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByCurp(String curp);

    Optional<Customer> findByRfc(String rfc);

    @Query("""
            select distinct c from Customer c
            left join CustomerWorkshop cw on cw.customer = c
            where (:workshopId is null or cw.workshop.id = :workshopId)
            """)
    Page<Customer> findForAdministration(@Param("workshopId") Long workshopId, Pageable pageable);

    @Query("""
            select c from Customer c
            where lower(c.email) = lower(:email)
               or upper(c.curp) = upper(:curp)
               or upper(c.rfc) = upper(:rfc)
            """)
    Optional<Customer> findExistingIdentity(
            @Param("curp") String curp,
            @Param("rfc") String rfc,
            @Param("email") String email
    );

    @Query("""
            select c from Customer c
            where c.id <> :customerId
              and (lower(c.email) = lower(:email)
                   or upper(c.curp) = upper(:curp)
                   or upper(c.rfc) = upper(:rfc))
            """)
    Optional<Customer> findDuplicatedIdentityForUpdate(
            @Param("customerId") Long customerId,
            @Param("curp") String curp,
            @Param("rfc") String rfc,
            @Param("email") String email
    );

    @Query("""
            select c from Customer c
            join CustomerWorkshop cw on cw.customer = c
            where cw.workshop.id in :workshopIds
            """)
    Page<Customer> findByWorkshopIds(@Param("workshopIds") Collection<Long> workshopIds, Pageable pageable);
}
