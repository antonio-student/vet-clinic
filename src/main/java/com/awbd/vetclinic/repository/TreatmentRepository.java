package com.awbd.vetclinic.repository;

import com.awbd.vetclinic.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByMedicalRecordIdOrderByTreatmentDateDescIdDesc(Long medicalRecordId);
}

