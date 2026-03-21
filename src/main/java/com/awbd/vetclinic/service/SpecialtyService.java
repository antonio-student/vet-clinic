package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Specialty;
import com.awbd.vetclinic.repository.SpecialtyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpecialtyService extends BaseService<Specialty> {

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        super(specialtyRepository, "Specialty");
    }

    public Specialty create(Specialty specialty) {
        log.info("Saving new specialty with name: {}", specialty.getName());
        return createEntity(specialty);
    }

    public Page<Specialty> getAllSpecialties(Pageable pageable) {
        log.info("Fetching all specialties with pagination");
        return getAllEntities(pageable);
    }

    public Specialty getSpecialtyById(Long id) {
        log.info("Fetching specialty with id: {}", id);
        return getEntityById(id);
    }

    public Specialty update(Long id, Specialty specialtyDetails) {
        log.info("Updating specialty with id: {}", id);

        return updateEntity(id, specialtyDetails, (existingSpecialty, details) -> {
            existingSpecialty.setName(details.getName());
            existingSpecialty.setDescription(details.getDescription());
        });
    }

    public void deleteSpecialty(Long id) {
        log.info("Deleting specialty with id: {}", id);
        deleteEntity(id);
    }
}

