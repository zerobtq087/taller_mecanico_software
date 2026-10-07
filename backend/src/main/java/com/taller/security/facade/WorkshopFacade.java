package com.taller.security.facade;

import com.taller.security.dto.WorkshopDtos.WorkshopRequest;
import com.taller.security.dto.WorkshopDtos.WorkshopResponse;
import com.taller.security.service.WorkshopService;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class WorkshopFacade {
    private final WorkshopService workshopService;

    public WorkshopFacade(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    public WorkshopResponse createWorkshop(WorkshopRequest request, MultipartFile photo) {
        return workshopService.createWorkshop(request, photo);
    }

    public WorkshopResponse updateWorkshop(Long id, WorkshopRequest request, MultipartFile photo) {
        return workshopService.updateWorkshop(id, request, photo);
    }

    public List<WorkshopResponse> listWorkshops() {
        return workshopService.listWorkshops();
    }
}
