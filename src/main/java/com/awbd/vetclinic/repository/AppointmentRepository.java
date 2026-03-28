package com.awbd.vetclinic.repository;

import com.awbd.vetclinic.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAnimalIdOrderByAppointmentDateDesc(Long animalId);
}

