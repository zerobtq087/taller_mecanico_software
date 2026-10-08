package com.taller.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "status_catalog", uniqueConstraints = {
        @UniqueConstraint(name = "uk_status_catalog_str_valor", columnNames = "str_valor")
})
public class StatusCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "str_valor", nullable = false, length = 40)
    private String strValor;

    @Column(name = "str_descripcion", nullable = false, length = 180)
    private String strDescripcion;

    public Long getId() {
        return id;
    }

    public String getStrValor() {
        return strValor;
    }

    public void setStrValor(String strValor) {
        this.strValor = strValor;
    }

    public String getStrDescripcion() {
        return strDescripcion;
    }

    public void setStrDescripcion(String strDescripcion) {
        this.strDescripcion = strDescripcion;
    }
}
