package com.awbd.vetclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Appointment date and time are mandatory")
    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    @NotBlank(message = "Reason for appointment is mandatory")
    private String reason;

    @NotBlank(message = "Status is mandatory (e.g., SCHEDULED, COMPLETED, CANCELLED)")
    private String status;

    // ManyToOne relationship: An appointment belongs to one doctor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // ManyToOne relationship: An appointment is made for one animal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;
}
