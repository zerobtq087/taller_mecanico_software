package com.taller.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "vehicle_makes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicle_makes_name", columnNames = "name")
})
public class VehicleMake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "make_group", length = 120)
    private String makeGroup;

    @Column(nullable = false, length = 120)
    private String name;

    public Long getId() {
        return id;
    }

    public String getMakeGroup() {
        return makeGroup;
    }

    public void setMakeGroup(String makeGroup) {
        this.makeGroup = makeGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
