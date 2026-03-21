package com.awbd.vetclinic.service;

import com.awbd.vetclinic.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.BiConsumer;

public abstract class BaseService<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JpaRepository<T, Long> repository;
    private final String entityName;

    protected BaseService(JpaRepository<T, Long> repository, String entityName) {
        this.repository = repository;
        this.entityName = entityName;
    }

    protected T createEntity(T entity) {
        return repository.save(entity);
    }

    protected Page<T> getAllEntities(Pageable pageable) {
        return repository.findAll(pageable);
    }

    protected T getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("{} with id {} not found", entityName, id);
                    return new EntityNotFoundException(entityName + " not found with id: " + id);
                });
    }

    protected T updateEntity(Long id, T entityDetails, BiConsumer<T, T> updater) {
        T existingEntity = getEntityById(id);
        updater.accept(existingEntity, entityDetails);
        return repository.save(existingEntity);
    }

    protected void deleteEntity(Long id) {
        T existingEntity = getEntityById(id);
        repository.delete(existingEntity);
    }
}

