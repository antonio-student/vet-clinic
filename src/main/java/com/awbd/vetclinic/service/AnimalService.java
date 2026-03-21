package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.repository.AnimalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnimalService extends BaseService<Animal> {

    public AnimalService(AnimalRepository animalRepository) {
        super(animalRepository, "Animal");
    }

    public Animal create(Animal animal) {
        log.info("Saving new animal with name: {}", animal.getName());
        return createEntity(animal);
    }

    public Page<Animal> getAllAnimals(Pageable pageable) {
        log.info("Fetching all animals with pagination");
        return getAllEntities(pageable);
    }

    public Animal getAnimalById(Long id) {
        log.info("Fetching animal with id: {}", id);
        return getEntityById(id);
    }

    public Animal update(Long id, Animal animalDetails) {
        log.info("Updating animal with id: {}", id);

        return updateEntity(id, animalDetails, (existingAnimal, details) -> {
            existingAnimal.setName(details.getName());
            existingAnimal.setSpecies(details.getSpecies());
            existingAnimal.setAge(details.getAge());
            // existingAnimal.setClient(details.getClient()); // Update relationship if needed
        });
    }

    public void deleteAnimal(Long id) {
        log.info("Deleting animal with id: {}", id);
        deleteEntity(id);
    }
}
