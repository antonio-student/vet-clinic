package com.awbd.vetclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Creation date cannot be null")
    @Column(nullable = false)
    private LocalDate creationDate;

    private String generalNotes;

    // (one record belongs to exactly one animal)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false, unique = true)
    private Animal animal;

    // (a medical record can contain multiple treatments)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Treatment> treatments = new ArrayList<>();

    public void addTreatment(Treatment treatment) {
        treatments.add(treatment);
        treatment.setMedicalRecord(this);
    }

    public void removeTreatment(Treatment treatment) {
        treatments.remove(treatment);
        treatment.setMedicalRecord(null);
    }
}