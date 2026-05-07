package com.awbd.vetclinic.service;

import com.awbd.vetclinic.exception.EntityNotFoundException;
import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.repository.AnimalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    private final Long animalId = 1L;
    @Mock
    private AnimalRepository animalRepository;
    private AnimalService animalService;
    private Animal animal;

    @BeforeEach
    void setUp() {
        animalService = new AnimalService(animalRepository);
        animal = new Animal();
        animal.setId(animalId);
        animal.setName("Rex");
        animal.setSpecies("Dog");
        animal.setAge(5);
    }

    @Test
    @DisplayName("Should save a new animal successfully")
    void create_ShouldSaveAnimal() {
        when(animalRepository.save(any(Animal.class))).thenReturn(animal);

        Animal savedAnimal = animalService.create(animal);

        assertNotNull(savedAnimal);
        assertEquals("Rex", savedAnimal.getName());
        verify(animalRepository, times(1)).save(animal);
    }

    @Test
    @DisplayName("Should return animal when valid ID is provided")
    void getAnimalById_WhenValidId_ShouldReturnAnimal() {
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        Animal foundAnimal = animalService.getAnimalById(animalId);

        assertNotNull(foundAnimal);
        assertEquals(animalId, foundAnimal.getId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when animal is not found")
    void getAnimalById_WhenInvalidId_ShouldThrowException() {
        when(animalRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> animalService.getAnimalById(99L));
    }

    @Test
    @DisplayName("Should update existing animal")
    void update_ShouldUpdateAnimal() {
        Animal details = new Animal();
        details.setName("Rex Updated");
        details.setSpecies("Wolf");
        details.setAge(6);

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        when(animalRepository.save(any(Animal.class))).thenReturn(animal);

        Animal updatedAnimal = animalService.update(animalId, details);

        assertEquals("Rex Updated", updatedAnimal.getName());
        assertEquals("Wolf", updatedAnimal.getSpecies());
        assertEquals(6, updatedAnimal.getAge());
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    @DisplayName("Should delete animal successfully")
    void deleteAnimal_ShouldDeleteAnimal() {
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        doNothing().when(animalRepository).delete(animal);

        animalService.deleteAnimal(animalId);

        verify(animalRepository, times(1)).delete(animal);
    }

    @Test
    @DisplayName("Should return a paged list of animals")
    void getAllAnimals_ShouldReturnPagedAnimals() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Animal> animalPage = new PageImpl<>(Collections.singletonList(animal));
        when(animalRepository.findAll(pageable)).thenReturn(animalPage);

        Page<Animal> result = animalService.getAllAnimals(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(animalRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should search animals with filters")
    @SuppressWarnings("unchecked")
    void searchAnimals_WithFilters_ShouldCallRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(animalRepository.findAll(nullable(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(animal)));

        Page<Animal> result = animalService.searchAnimals("Rex", "Dog", pageable);

        assertNotNull(result);
        verify(animalRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should search animals with empty filters")
    @SuppressWarnings("unchecked")
    void searchAnimals_WithEmptyFilters_ShouldCallRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(animalRepository.findAll(nullable(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(animal)));

        Page<Animal> result = animalService.searchAnimals("", null, pageable);

        assertNotNull(result);
        verify(animalRepository).findAll((org.springframework.data.jpa.domain.Specification<Animal>) isNull(), eq(pageable));
    }
}
