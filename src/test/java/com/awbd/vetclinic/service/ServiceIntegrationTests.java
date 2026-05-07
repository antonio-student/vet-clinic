package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceIntegrationTests {

    @Autowired
    private ClientService clientService;
    @Autowired
    private AnimalService animalService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private TreatmentService treatmentService;

    @Test
    @DisplayName("Scenario 1: Complete Appointment Flow")
    void integration_AppointmentFlow() {
        // 1. Create a Client
        Client client = new Client();
        client.setName("Integration Client");
        client.setEmail("int@test.com");
        client.setPhone("0700000000");
        Client savedClient = clientService.create(client);

        // 2. Create an Animal for that client
        Animal animal = new Animal();
        animal.setName("IntAnimal");
        animal.setSpecies("Cat");
        animal.setClient(savedClient);
        Animal savedAnimal = animalService.create(animal);

        // 3. Create a Doctor
        Doctor doctor = new Doctor();
        doctor.setFirstName("Int");
        doctor.setLastName("Doctor");
        doctor.setEmail("doc@test.com");
        Doctor savedDoctor = doctorService.create(doctor);

        // 4. Schedule an Appointment
        Appointment appointment = new Appointment();
        appointment.setAnimal(savedAnimal);
        appointment.setDoctor(savedDoctor);
        appointment.setAppointmentDate(LocalDateTime.now().plusDays(1));
        appointment.setReason("Annual checkup");
        appointment.setStatus("SCHEDULED");
        Appointment savedApp = appointmentService.create(appointment);

        // Verify
        assertNotNull(savedApp.getId());
        List<Appointment> animalApps = appointmentService.getAppointmentsForAnimal(savedAnimal.getId());
        assertEquals(1, animalApps.size());
        assertEquals("Annual checkup", animalApps.getFirst().getReason());
    }

    @Test
    @DisplayName("Scenario 2: Medical History Management Flow")
    void integration_MedicalHistoryFlow() {
        // 0. Create Client (Required for Animal)
        Client client = new Client();
        client.setName("Owner X");
        client.setEmail("ownerx@test.com");
        client.setPhone("0711111111");
        Client savedClient = clientService.create(client);

        // 1. Create Animal
        Animal animal = new Animal();
        animal.setName("Patient X");
        animal.setSpecies("Dog");
        animal.setClient(savedClient);
        Animal savedAnimal = animalService.create(animal);

        // 2. Create Medical Record for Animal
        MedicalRecord record = new MedicalRecord();
        record.setAnimal(savedAnimal);
        record.setGeneralNotes("Initial check");
        record.setCreationDate(LocalDate.now());
        MedicalRecord savedRecord = medicalRecordService.create(record);

        // 3. Add multiple Treatments to that record
        Treatment t1 = new Treatment();
        t1.setMedicalRecord(savedRecord);
        t1.setDescription("Vaccine A");
        t1.setTreatmentDate(LocalDate.now());
        treatmentService.create(t1);

        Treatment t2 = new Treatment();
        t2.setMedicalRecord(savedRecord);
        t2.setDescription("Vaccine B");
        t2.setTreatmentDate(LocalDate.now().plusDays(1));
        treatmentService.create(t2);

        // Verify
        List<Treatment> treatments = treatmentService.getTreatmentsForMedicalRecord(savedRecord.getId());
        assertEquals(2, treatments.size());
        assertTrue(treatments.stream().anyMatch(t -> t.getDescription().equals("Vaccine A")));
    }

    @Test
    @DisplayName("Scenario 3: Animal Update and Search Flow")
    void integration_SearchAndFilterFlow() {
        // 0. Create Client (Required for Animal)
        Client client = new Client();
        client.setName("Search Client");
        client.setEmail("search@test.com");
        client.setPhone("0722222222");
        Client savedClient = clientService.create(client);

        // 1. Setup multiple animals
        Animal a1 = new Animal();
        a1.setName("Alpha");
        a1.setSpecies("Dog");
        a1.setClient(savedClient);
        animalService.create(a1);
        Animal a2 = new Animal();
        a2.setName("Beta");
        a2.setSpecies("Cat");
        a2.setClient(savedClient);
        animalService.create(a2);
        Animal a3 = new Animal();
        a3.setName("Gamma");
        a3.setSpecies("Dog");
        a3.setClient(savedClient);
        animalService.create(a3);

        // 2. Search by species
        var dogPage = animalService.searchAnimals("", "Dog", org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(2, dogPage.getTotalElements());

        // 3. Update an animal and verify search again
        Animal beta = animalService.searchAnimals("Beta", "", org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent().getFirst();
        beta.setSpecies("Dog");
        animalService.update(beta.getId(), beta);

        var updatedDogPage = animalService.searchAnimals("", "Dog", org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(3, updatedDogPage.getTotalElements());
    }
}
