package com.awbd.vetclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "treatment")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Treatment description is mandatory")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "Treatment date is mandatory")
    private LocalDate treatmentDate;

    @Min(value = 0, message = "Cost cannot be negative")
    private double cost;

    // ManyToOne relationship: Many treatments belong to one medical record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;
}
