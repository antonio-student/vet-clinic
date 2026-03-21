package com.awbd.vetclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "animal")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Animal name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Species is required (e.g., Dog, Cat)")
    private String species;

    @Min(value = 0, message = "Age cannot be negative")
    private int age;

    // Many animals can belong to the same client.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // One animal can have multiple appointments.
    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    // Constructors
    public Animal() {
    }

    public Animal(String name, String species, int age, Client client) {
        this.name = name;
        this.species = species;
        this.age = age;
        this.client = client;
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setAnimal(this);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setAnimal(null);
    }
}