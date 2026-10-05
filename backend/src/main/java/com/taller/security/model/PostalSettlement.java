package com.taller.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "postal_settlements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_postal_settlement_source", columnNames = "source_settlement_id")
})
public class PostalSettlement {
    @Id
    private Long id;

    @Column(name = "source_settlement_id", nullable = false)
    private Long sourceSettlementId;

    @Column(name = "postal_code", nullable = false, length = 5)
    private String postalCode;

    @Column(name = "settlement_name", nullable = false, length = 180)
    private String settlementName;

    @Column(name = "settlement_type", nullable = false, length = 80)
    private String settlementType;

    @Column(name = "municipality_name", nullable = false, length = 140)
    private String municipalityName;

    @Column(name = "state_name", nullable = false, length = 140)
    private String stateName;

    @Column(name = "city_name", length = 140)
    private String cityName;

    @Column(name = "municipal_identifier", nullable = false, length = 20)
    private String municipalIdentifier;

    public Long getId() {
        return id;
    }

    public Long getSourceSettlementId() {
        return sourceSettlementId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getSettlementName() {
        return settlementName;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public String getStateName() {
        return stateName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getMunicipalIdentifier() {
        return municipalIdentifier;
    }
}
