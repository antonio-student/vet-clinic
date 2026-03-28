package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.model.MedicalRecord;
import com.awbd.vetclinic.model.Treatment;
import com.awbd.vetclinic.service.AnimalService;
import com.awbd.vetclinic.service.AppointmentService;
import com.awbd.vetclinic.service.ClientService;
import com.awbd.vetclinic.service.MedicalRecordService;
import com.awbd.vetclinic.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/animals")
public class AnimalController {
    private final AnimalService animalService;
    private final ClientService clientService;
    private final MedicalRecordService medicalRecordService;
    private final TreatmentService treatmentService;
    private final AppointmentService appointmentService;

    public AnimalController(AnimalService animalService,
                            ClientService clientService,
                            MedicalRecordService medicalRecordService,
                            TreatmentService treatmentService,
                            AppointmentService appointmentService) {
        this.animalService = animalService;
        this.clientService = clientService;
        this.medicalRecordService = medicalRecordService;
        this.treatmentService = treatmentService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String listAnimals(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show animals page: {}", page);
        Page<Animal> animalPage = animalService.getAllAnimals(PageRequest.of(page, 7));

        model.addAttribute("animalPage", animalPage);
        model.addAttribute("currentPage", page);
        return "animals/list"; // This matches the Thymeleaf template path
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Animal animal = new Animal();
        animal.setClient(new Client());
        model.addAttribute("animal", animal);
        model.addAttribute("hasMedicalRecord", false);
        populateFormOptions(model);
        return "animals/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("animal") Animal animal, BindingResult bindingResult, Model model) {
        if (animal.getClient() == null || animal.getClient().getId() == null) {
            bindingResult.rejectValue("client", "animal.client", "Owner is required");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for animal creation");
            if (animal.getClient() == null) {
                animal.setClient(new Client());
            }
            model.addAttribute("hasMedicalRecord", animal.getId() != null && medicalRecordService.findByAnimalId(animal.getId()).isPresent());
            populateFormOptions(model);
            return "animals/form"; // Return to form to show validation messages
        }

        animal.setClient(clientService.getClientById(animal.getClient().getId()));
        Animal savedAnimal = animalService.create(animal);
        return "redirect:/animals/" + savedAnimal.getId();
    }

    @GetMapping("/{id}")
    public String showProfile(@PathVariable Long id, Model model) {
        Animal animal = animalService.getAnimalById(id);
        populatePatientChart(model, animal, new QuickTreatmentForm());
        return "animals/profile";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Animal animal = animalService.getAnimalById(id);
        model.addAttribute("animal", animal);
        model.addAttribute("hasMedicalRecord", medicalRecordService.findByAnimalId(id).isPresent());
        populateFormOptions(model);
        return "animals/form";
    }

    @PostMapping("/{id}/medical-record")
    public String createMedicalRecord(@PathVariable Long id) {
        Animal animal = animalService.getAnimalById(id);
        if (medicalRecordService.findByAnimalId(id).isEmpty()) {
            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setAnimal(animal);
            medicalRecord.setCreationDate(java.time.LocalDate.now());
            medicalRecord.setGeneralNotes("Patient chart created from the animal profile.");
            medicalRecordService.create(medicalRecord);
        }
        return "redirect:/animals/" + id;
    }

    @PostMapping("/{id}/treatments")
    public String addTreatment(@PathVariable Long id,
                               @Valid @ModelAttribute("quickTreatment") QuickTreatmentForm quickTreatment,
                               BindingResult bindingResult,
                               Model model) {
        Animal animal = animalService.getAnimalById(id);
        MedicalRecord medicalRecord = medicalRecordService.findByAnimalId(id).orElse(null);

        if (medicalRecord == null) {
            bindingResult.reject("medicalRecord", "Create the medical record before adding treatments.");
        }

        if (bindingResult.hasErrors()) {
            populatePatientChart(model, animal, quickTreatment);
            return "animals/profile";
        }

        Treatment treatment = new Treatment();
        treatment.setDescription(quickTreatment.getDescription());
        treatment.setTreatmentDate(quickTreatment.getTreatmentDate());
        treatment.setCost(quickTreatment.getCost());
        treatment.setMedicalRecord(medicalRecord);
        treatmentService.create(treatment);
        return "redirect:/animals/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return "redirect:/animals";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("clients", clientService.getAllClients(Pageable.unpaged()).getContent());
    }

    private void populatePatientChart(Model model, Animal animal, QuickTreatmentForm quickTreatment) {
        var medicalRecordOptional = medicalRecordService.findByAnimalId(animal.getId());
        model.addAttribute("animal", animal);
        model.addAttribute("medicalRecord", medicalRecordOptional.orElse(null));
        model.addAttribute("treatments", medicalRecordOptional
                .map(record -> treatmentService.getTreatmentsForMedicalRecord(record.getId()))
                .orElseGet(java.util.List::of));
        model.addAttribute("appointments", appointmentService.getAppointmentsForAnimal(animal.getId()));
        model.addAttribute("quickTreatment", quickTreatment);
        model.addAttribute("hasMedicalRecord", medicalRecordOptional.isPresent());
    }
}
