package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Treatment;
import com.awbd.vetclinic.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    private final Long treatmentId = 1L;
    @Mock
    private TreatmentRepository treatmentRepository;
    private TreatmentService treatmentService;
    private Treatment treatment;

    @BeforeEach
    void setUp() {
        treatmentService = new TreatmentService(treatmentRepository);
        treatment = new Treatment();
        treatment.setId(treatmentId);
        treatment.setDescription("Antibiotics");
        treatment.setCost(150.00d);
        treatment.setTreatmentDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should create a treatment")
    void create_ShouldSave() {
        when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);
        Treatment saved = treatmentService.create(treatment);
        assertNotNull(saved);
        assertEquals("Antibiotics", saved.getDescription());
        verify(treatmentRepository).save(treatment);
    }

    @Test
    @DisplayName("Should update treatment")
    void update_ShouldUpdate() {
        Treatment details = new Treatment();
        details.setDescription("Updated Treatment");
        details.setCost(200.00d);

        when(treatmentRepository.findById(treatmentId)).thenReturn(Optional.of(treatment));
        when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);

        Treatment updated = treatmentService.update(treatmentId, details);

        assertEquals("Updated Treatment", updated.getDescription());
        assertEquals(200.00d, updated.getCost());
    }
}
