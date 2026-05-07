package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    private final Long doctorId = 1L;
    @Mock
    private DoctorRepository doctorRepository;
    private DoctorService doctorService;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(doctorRepository);
        doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setFirstName("John");
        doctor.setLastName("Doe");
        doctor.setEmail("john.doe@vet.com");
    }

    @Test
    @DisplayName("Should create a doctor")
    void create_ShouldSaveDoctor() {
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        Doctor saved = doctorService.create(doctor);
        assertNotNull(saved);
        assertEquals("John", saved.getFirstName());
        verify(doctorRepository).save(doctor);
    }

    @Test
    @DisplayName("Should update doctor details")
    void update_ShouldUpdateDoctor() {
        Doctor details = new Doctor();
        details.setFirstName("Jane");
        details.setLastName("Smith");
        details.setEmail("jane.smith@vet.com");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        Doctor updated = doctorService.update(doctorId, details);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("Should delete doctor")
    void deleteDoctor_ShouldDelete() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doctorService.deleteDoctor(doctorId);
        verify(doctorRepository).delete(doctor);
    }
}
