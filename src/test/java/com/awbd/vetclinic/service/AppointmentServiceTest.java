package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private final Long appointmentId = 1L;
    @Mock
    private AppointmentRepository appointmentRepository;
    private AppointmentService appointmentService;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository);
        appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setAppointmentDate(LocalDateTime.now().plusDays(1));
        appointment.setReason("General Checkup");
        appointment.setStatus("SCHEDULED");
    }

    @Test
    @DisplayName("Should create an appointment")
    void create_ShouldSave() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        Appointment saved = appointmentService.create(appointment);
        assertNotNull(saved);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Should update appointment")
    void update_ShouldUpdate() {
        Appointment details = new Appointment();
        details.setReason("Vaccination");
        details.setStatus("COMPLETED");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Appointment updated = appointmentService.update(appointmentId, details);

        assertEquals("Vaccination", updated.getReason());
        assertEquals("COMPLETED", updated.getStatus());
    }
}
