package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Specialty;
import com.awbd.vetclinic.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    private final Long specialtyId = 1L;
    @Mock
    private SpecialtyRepository specialtyRepository;
    private SpecialtyService specialtyService;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialtyService = new SpecialtyService(specialtyRepository);
        specialty = new Specialty();
        specialty.setId(specialtyId);
        specialty.setName("Surgery");
    }

    @Test
    @DisplayName("Should create specialty")
    void create_ShouldSave() {
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        Specialty saved = specialtyService.create(specialty);
        assertNotNull(saved);
        assertEquals("Surgery", saved.getName());
    }

    @Test
    @DisplayName("Should find specialty by name ignore case")
    void findByNameIgnoreCase_ShouldReturnOptional() {
        when(specialtyRepository.findByNameIgnoreCase("surgery")).thenReturn(Optional.of(specialty));
        Optional<Specialty> found = specialtyService.findByNameIgnoreCase("surgery");
        assertTrue(found.isPresent());
        assertEquals("Surgery", found.get().getName());
    }

    @Test
    @DisplayName("Should get existing specialty by name")
    void getOrCreateByName_WhenExists_ShouldReturnExisting() {
        when(specialtyRepository.findByNameIgnoreCase("Surgery")).thenReturn(Optional.of(specialty));
        Specialty result = specialtyService.getOrCreateByName("Surgery");
        assertEquals(specialtyId, result.getId());
        verify(specialtyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new specialty when not found by name")
    void getOrCreateByName_WhenNotExists_ShouldCreateNew() {
        when(specialtyRepository.findByNameIgnoreCase("Cardiology")).thenReturn(Optional.empty());
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Specialty result = specialtyService.getOrCreateByName("Cardiology");

        assertNotNull(result);
        assertEquals("Cardiology", result.getName());
        verify(specialtyRepository).save(any(Specialty.class));
    }
}
