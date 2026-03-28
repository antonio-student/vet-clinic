package com.awbd.vetclinic.repository;

import com.awbd.vetclinic.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByNameIgnoreCase(String name);
}

