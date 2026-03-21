package com.awbd.vetclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "specialty")
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Specialty name is mandatory")
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // The inverse side of the ManyToMany relationship
    @ManyToMany(mappedBy = "specialties")
    private List<Doctor> doctors = new ArrayList<>();
}
