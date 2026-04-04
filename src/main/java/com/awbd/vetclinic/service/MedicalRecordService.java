package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.MedicalRecord;
import com.awbd.vetclinic.repository.MedicalRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MedicalRecordService extends BaseService<MedicalRecord> {
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        super(medicalRecordRepository, "Medical record");
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public MedicalRecord create(MedicalRecord medicalRecord) {
        log.info("Saving new medical record");
        return createEntity(medicalRecord);
    }

    public Page<MedicalRecord> getAllMedicalRecords(Pageable pageable) {
        log.info("Fetching all medical records with pagination");
        return getAllEntities(pageable);
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        log.info("Fetching medical record with id: {}", id);
        return getEntityById(id);
    }

    public Optional<MedicalRecord> findByAnimalId(Long animalId) {
        log.info("Fetching medical record for animal with id: {}", animalId);
        return medicalRecordRepository.findByAnimalId(animalId);
    }

    public MedicalRecord update(Long id, MedicalRecord medicalRecordDetails) {
        log.info("Updating medical record with id: {}", id);

        return updateEntity(id, medicalRecordDetails, (existingMedicalRecord, details) -> {
            existingMedicalRecord.setCreationDate(details.getCreationDate());
            existingMedicalRecord.setGeneralNotes(details.getGeneralNotes());
            existingMedicalRecord.setAnimal(details.getAnimal());
        });
    }

    public void deleteMedicalRecord(Long id) {
        log.info("Deleting medical record with id: {}", id);
        deleteEntity(id);
    }
}

