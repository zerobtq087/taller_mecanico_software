package com.taller.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "vehicles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicles_vin", columnNames = "vin"),
        @UniqueConstraint(name = "uk_vehicles_plate", columnNames = "plate")
})
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false, length = 12)
    private String plate;

    @Column(nullable = false, length = 120)
    private String make;

    @Column(nullable = false, length = 160)
    private String model;

    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Column(nullable = false, length = 260)
    private String version;

    @Column(nullable = false, length = 80)
    private String color;

    @Column
    private Integer mileage;

    @Column(name = "serial_number", nullable = false, length = 80)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private StatusCatalog status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public StatusCatalog getStatus() { return status; }
    public void setStatus(StatusCatalog status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
