package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Specialty;
import com.awbd.vetclinic.repository.SpecialtyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class SpecialtyService extends BaseService<Specialty> {
    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        super(specialtyRepository, "Specialty");
        this.specialtyRepository = specialtyRepository;
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

    public Optional<Specialty> findByNameIgnoreCase(String name) {
        log.info("Fetching specialty with name: {}", name);
        return specialtyRepository.findByNameIgnoreCase(name);
    }

    public Specialty getOrCreateByName(String name) {
        String normalizedName = name == null ? "" : name.trim();
        return findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> {
                    Specialty specialty = new Specialty();
                    specialty.setName(normalizedName);
                    specialty.setDescription(null);
                    return create(specialty);
                });
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

