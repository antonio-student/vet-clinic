package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.repository.DoctorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DoctorService extends BaseService<Doctor> {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        super(doctorRepository, "Doctor");
        this.doctorRepository = doctorRepository;
    }

    public Doctor create(Doctor doctor) {
        log.info("Saving new doctor: {} {}", doctor.getFirstName(), doctor.getLastName());
        return createEntity(doctor);
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        log.info("Fetching all doctors with pagination");
        return getAllEntities(pageable);
    }

    public Page<Doctor> searchDoctors(String name, String email, Pageable pageable) {
        log.info("Searching doctors with filters name={}, email={}", name, email);
        List<Specification<Doctor>> specifications = new ArrayList<>();
        addIfPresent(specifications, nameContains(name));
        addIfPresent(specifications, containsIgnoreCase("email", email));
        Specification<Doctor> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse(null);
        assert specification != null;
        return doctorRepository.findAll(specification, pageable);
    }

    public Doctor getDoctorById(Long id) {
        log.info("Fetching doctor with id: {}", id);
        return getEntityById(id);
    }

    public Doctor update(Long id, Doctor doctorDetails) {
        log.info("Updating doctor with id: {}", id);

        return updateEntity(id, doctorDetails, (existingDoctor, details) -> {
            existingDoctor.setFirstName(details.getFirstName());
            existingDoctor.setLastName(details.getLastName());
            existingDoctor.setEmail(details.getEmail());
            existingDoctor.setSpecialties(details.getSpecialties());
        });
    }

    public void deleteDoctor(Long id) {
        log.info("Deleting doctor with id: {}", id);
        deleteEntity(id);
    }

    private Specification<Doctor> nameContains(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String searchValue = "%" + value.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchValue),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchValue)
        );
    }

    private Specification<Doctor> containsIgnoreCase(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.trim().toLowerCase() + "%"
        );
    }

    private void addIfPresent(List<Specification<Doctor>> specifications, Specification<Doctor> specification) {
        if (specification != null) {
            specifications.add(specification);
        }
    }
}

