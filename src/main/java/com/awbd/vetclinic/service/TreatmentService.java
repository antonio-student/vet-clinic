package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Treatment;
import com.awbd.vetclinic.repository.TreatmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TreatmentService extends BaseService<Treatment> {
    private final TreatmentRepository treatmentRepository;

    public TreatmentService(TreatmentRepository treatmentRepository) {
        super(treatmentRepository, "Treatment");
        this.treatmentRepository = treatmentRepository;
    }

    public Treatment create(Treatment treatment) {
        log.info("Saving new treatment");
        return createEntity(treatment);
    }

    public Page<Treatment> getAllTreatments(Pageable pageable) {
        log.info("Fetching all treatments with pagination");
        return getAllEntities(pageable);
    }

    public Treatment getTreatmentById(Long id) {
        log.info("Fetching treatment with id: {}", id);
        return getEntityById(id);
    }

    public List<Treatment> getTreatmentsForMedicalRecord(Long medicalRecordId) {
        log.info("Fetching treatments for medical record with id: {}", medicalRecordId);
        return treatmentRepository.findByMedicalRecordIdOrderByTreatmentDateDescIdDesc(medicalRecordId);
    }

    public Treatment update(Long id, Treatment treatmentDetails) {
        log.info("Updating treatment with id: {}", id);

        return updateEntity(id, treatmentDetails, (existingTreatment, details) -> {
            existingTreatment.setDescription(details.getDescription());
            existingTreatment.setTreatmentDate(details.getTreatmentDate());
            existingTreatment.setCost(details.getCost());
            existingTreatment.setMedicalRecord(details.getMedicalRecord());
        });
    }

    public void deleteTreatment(Long id) {
        log.info("Deleting treatment with id: {}", id);
        deleteEntity(id);
    }
}

