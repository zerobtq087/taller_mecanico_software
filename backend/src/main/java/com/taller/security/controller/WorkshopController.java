package com.taller.security.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkshopController {
    @GetMapping("/api/me")
    public Map<String, String> me(java.security.Principal principal) {
        return Map.of("email", principal.getName());
    }

    @GetMapping("/api/taller/ordenes")
    public List<Map<String, Object>> workOrders() {
        return List.of(
                Map.of("folio", "OT-1042", "vehiculo", "Nissan Versa", "estado", "Diagnostico"),
                Map.of("folio", "OT-1043", "vehiculo", "VW Jetta", "estado", "En reparacion"),
                Map.of("folio", "OT-1044", "vehiculo", "Toyota Hilux", "estado", "Listo para entrega")
        );
    }

    @GetMapping("/api/secretaria/citas")
    public List<Map<String, Object>> appointments() {
        return List.of(
                Map.of("cliente", "Ana Lopez", "hora", "09:30", "servicio", "Afinacion"),
                Map.of("cliente", "Marco Ruiz", "hora", "11:00", "servicio", "Frenos")
        );
    }

    @GetMapping("/api/admin/reportes")
    public Map<String, Object> reports() {
        return Map.of("ordenesActivas", 18, "ingresoEstimado", 78200, "usuariosActivos", 64);
    }
}
