package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.repository.AnimalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AnimalService extends BaseService<Animal> {
    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        super(animalRepository, "Animal");
        this.animalRepository = animalRepository;
    }

    public Animal create(Animal animal) {
        log.info("Saving new animal with name: {}", animal.getName());
        return createEntity(animal);
    }

    public Page<Animal> getAllAnimals(Pageable pageable) {
        log.info("Fetching all animals with pagination");
        return getAllEntities(pageable);
    }

    public Page<Animal> searchAnimals(String name, String species, Pageable pageable) {
        log.info("Searching animals with filters name={}, species={}", name, species);
        List<Specification<Animal>> specifications = new ArrayList<>();
        addIfPresent(specifications, containsIgnoreCase("name", name));
        addIfPresent(specifications, containsIgnoreCase("species", species));
        Specification<Animal> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse(null);
        return animalRepository.findAll(specification, pageable);
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
            existingAnimal.setClient(details.getClient());
        });
    }

    public void deleteAnimal(Long id) {
        log.info("Deleting animal with id: {}", id);
        deleteEntity(id);
    }

    private Specification<Animal> containsIgnoreCase(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.trim().toLowerCase() + "%"
        );
    }

    private Specification<Animal> ownedByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        String normalizedUsername = username.trim().toLowerCase();
        return (root, query, criteriaBuilder) -> {
            var emailPath = criteriaBuilder.lower(root.join("client").get("email"));
            return criteriaBuilder.or(
                    criteriaBuilder.equal(emailPath, normalizedUsername),
                    criteriaBuilder.like(emailPath, normalizedUsername + "@%")
            );
        };
    }

    private void addIfPresent(List<Specification<Animal>> specifications, Specification<Animal> specification) {
        if (specification != null) {
            specifications.add(specification);
        }
    }
}
