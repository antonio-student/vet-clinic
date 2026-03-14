package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.repository.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppointmentService extends BaseService<Appointment> {

    public AppointmentService(AppointmentRepository appointmentRepository) {
        super(appointmentRepository, "Appointment");
    }

    public Appointment create(Appointment appointment) {
        log.info("Saving new appointment");
        return createEntity(appointment);
    }

    public Page<Appointment> getAllAppointments(Pageable pageable) {
        log.info("Fetching all appointments with pagination");
        return getAllEntities(pageable);
    }

    public Appointment getAppointmentById(Long id) {
        log.info("Fetching appointment with id: {}", id);
        return getEntityById(id);
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
}

