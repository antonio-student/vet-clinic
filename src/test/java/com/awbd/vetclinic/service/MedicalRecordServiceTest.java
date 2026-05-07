package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.MedicalRecord;
import com.awbd.vetclinic.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    private final Long recordId = 1L;
    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    private MedicalRecordService medicalRecordService;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        medicalRecordService = new MedicalRecordService(medicalRecordRepository);
        medicalRecord = new MedicalRecord();
        medicalRecord.setId(recordId);
        medicalRecord.setCreationDate(LocalDate.now());
        medicalRecord.setGeneralNotes("Healthy animal");
    }

    @Test
    @DisplayName("Should create medical record")
    void create_ShouldSave() {
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);
        MedicalRecord saved = medicalRecordService.create(medicalRecord);
        assertNotNull(saved);
        assertEquals("Healthy animal", saved.getGeneralNotes());
    }

    @Test
    @DisplayName("Should find medical record by animal id")
    void findByAnimalId_ShouldReturnOptional() {
        when(medicalRecordRepository.findByAnimalId(10L)).thenReturn(Optional.of(medicalRecord));
        Optional<MedicalRecord> found = medicalRecordService.findByAnimalId(10L);
        assertTrue(found.isPresent());
        assertEquals(recordId, found.get().getId());
    }

    @Test
    @DisplayName("Should update medical record")
    void update_ShouldUpdate() {
        MedicalRecord details = new MedicalRecord();
        details.setGeneralNotes("Updated notes");

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(medicalRecord));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        MedicalRecord updated = medicalRecordService.update(recordId, details);

        assertEquals("Updated notes", updated.getGeneralNotes());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }
}
