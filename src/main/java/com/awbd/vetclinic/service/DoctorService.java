package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.repository.DoctorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoctorService extends BaseService<Doctor> {

    public DoctorService(DoctorRepository doctorRepository) {
        super(doctorRepository, "Doctor");
    }

    public Doctor create(Doctor doctor) {
        log.info("Saving new doctor: {} {}", doctor.getFirstName(), doctor.getLastName());
        return createEntity(doctor);
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        log.info("Fetching all doctors with pagination");
        return getAllEntities(pageable);
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
        });
    }

    public void deleteDoctor(Long id) {
        log.info("Deleting doctor with id: {}", id);
        deleteEntity(id);
    }
}

