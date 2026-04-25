package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.repository.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AppointmentService extends BaseService<Appointment> {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        super(appointmentRepository, "Appointment");
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment create(Appointment appointment) {
        log.info("Saving new appointment");
        return createEntity(appointment);
    }

    public Page<Appointment> getAllAppointments(Pageable pageable) {
        log.info("Fetching all appointments with pagination");
        return getAllEntities(pageable);
    }

    public Page<Appointment> searchAppointments(Long animalId, LocalDate appointmentDate, Pageable pageable) {
        log.info("Searching appointments with filters animalId={}, appointmentDate={}", animalId, appointmentDate);
        List<Specification<Appointment>> specifications = new ArrayList<>();
        addIfPresent(specifications, hasAnimalId(animalId));
        addIfPresent(specifications, hasAppointmentDate(appointmentDate));
        Specification<Appointment> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse(null);
        assert specification != null;
        return appointmentRepository.findAll(specification, pageable);
    }

    public Appointment getAppointmentById(Long id) {
        log.info("Fetching appointment with id: {}", id);
        return getEntityById(id);
    }

    public List<Appointment> getAppointmentsForAnimal(Long animalId) {
        log.info("Fetching appointments for animal with id: {}", animalId);
        return appointmentRepository.findByAnimalIdOrderByAppointmentDateDesc(animalId);
    }

    public Appointment update(Long id, Appointment appointmentDetails) {
        log.info("Updating appointment with id: {}", id);

        return updateEntity(id, appointmentDetails, (existingAppointment, details) -> {
            existingAppointment.setAppointmentDate(details.getAppointmentDate());
            existingAppointment.setReason(details.getReason());
            existingAppointment.setStatus(details.getStatus());
            existingAppointment.setDoctor(details.getDoctor());
            existingAppointment.setAnimal(details.getAnimal());
        });
    }

    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with id: {}", id);
        deleteEntity(id);
    }

    private Specification<Appointment> hasAnimalId(Long animalId) {
        if (animalId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.join("animal").get("id"), animalId);
    }

    private Specification<Appointment> hasAppointmentDate(LocalDate appointmentDate) {
        if (appointmentDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get("appointmentDate"),
                appointmentDate.atStartOfDay(),
                appointmentDate.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    private Specification<Appointment> ownedByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        String normalizedUsername = username.trim().toLowerCase();
        return (root, query, criteriaBuilder) -> {
            var emailPath = criteriaBuilder.lower(root.join("animal").join("client").get("email"));
            return criteriaBuilder.or(
                    criteriaBuilder.equal(emailPath, normalizedUsername),
                    criteriaBuilder.like(emailPath, normalizedUsername + "@%")
            );
        };
    }

    private void addIfPresent(List<Specification<Appointment>> specifications, Specification<Appointment> specification) {
        if (specification != null) {
            specifications.add(specification);
        }
    }
}

