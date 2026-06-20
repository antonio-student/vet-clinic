package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.model.MedicalRecord;
import com.awbd.vetclinic.model.Treatment;
import com.awbd.vetclinic.service.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
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
    private final AccessControlService accessControlService;

    public AnimalController(AnimalService animalService,
                            ClientService clientService,
                            MedicalRecordService medicalRecordService,
                            TreatmentService treatmentService,
                            AppointmentService appointmentService,
                            AccessControlService accessControlService) {
        this.animalService = animalService;
        this.clientService = clientService;
        this.medicalRecordService = medicalRecordService;
        this.treatmentService = treatmentService;
        this.appointmentService = appointmentService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public String listAnimals(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String name,
                              @RequestParam(defaultValue = "") String species,
                              @RequestParam(defaultValue = "name") String sort,
                              @RequestParam(defaultValue = "asc") String dir,
                              Model model,
                              Authentication authentication) {
        log.info("Request to show animals page: {}, name: {}, species: {}, sort: {}, dir: {}", page, name, species, sort, dir);
        
        String ownerUsername = null;
        if (authentication != null && !accessControlService.isEmployee(authentication) && !accessControlService.isAdmin(authentication)) {
            ownerUsername = authentication.getName();
        }

        Page<Animal> animalPage = animalService.searchAnimals(
                name,
                species,
                ownerUsername,
                PageRequest.of(page, 7, buildSort(sort, dir))
        );

        model.addAttribute("animalPage", animalPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("nameFilter", name);
        model.addAttribute("speciesFilter", species);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        return "animals/list"; // This matches the Thymeleaf template path
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        Animal animal = new Animal();
        animal.setClient(new Client());
        return showForm(model, animal, false, authentication);
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("animal") Animal animal, BindingResult bindingResult, Model model, Authentication authentication) {
        if (animal.getClient() == null) {
            animal.setClient(new Client());
            bindingResult.rejectValue("client", "animal.client", "Owner details are required");
        } else if (animal.getClient().getId() == null) {
            // Dynamic client creation validation
            if (animal.getClient().getName() == null || animal.getClient().getName().isBlank()) {
                bindingResult.rejectValue("client.name", "NotBlank", "Owner name is required");
            }
            if (animal.getClient().getPhone() == null || animal.getClient().getPhone().isBlank()) {
                bindingResult.rejectValue("client.phone", "NotBlank", "Owner phone number is required");
            }
            String email = animal.getClient().getEmail();
            if (email == null || email.isBlank()) {
                bindingResult.rejectValue("client.email", "NotBlank", "Owner email is required");
            } else if (!email.contains("@")) {
                bindingResult.rejectValue("client.email", "Email", "Invalid email format");
            } else if (authentication != null && !accessControlService.isEmployee(authentication) && !accessControlService.isAdmin(authentication)) {
                if (!accessControlService.matchesUsername(email, authentication.getName())) {
                    bindingResult.rejectValue("client.email", "Security", "Email must belong to your user account");
                }
            }
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for animal creation");
            return showForm(model, animal, animal.getId() != null && medicalRecordService.findByAnimalId(animal.getId()).isPresent(), authentication);
        }

        if (animal.getClient().getId() == null) {
            Client newClient = new Client();
            newClient.setName(animal.getClient().getName());
            newClient.setPhone(animal.getClient().getPhone());
            newClient.setEmail(animal.getClient().getEmail());
            Client savedClient = clientService.create(newClient);
            animal.setClient(savedClient);
        } else {
            animal.setClient(clientService.getClientById(animal.getClient().getId()));
        }

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
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Animal animal = animalService.getAnimalById(id);
        return showForm(model, animal, medicalRecordService.findByAnimalId(id).isPresent(), authentication);
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("animal") Animal animal,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication) {
        if (animal.getClient() == null || animal.getClient().getId() == null) {
            bindingResult.rejectValue("client", "animal.client", "Owner is required");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for animal update with id: {}", id);
            animal.setId(id);
            if (animal.getClient() == null) {
                animal.setClient(new Client());
            }
            return showForm(model, animal, medicalRecordService.findByAnimalId(id).isPresent(), authentication);
        }

        animal.setClient(clientService.getClientById(animal.getClient().getId()));
        animalService.update(id, animal);
        return "redirect:/animals?updated=true";
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

    private String showForm(Model model, Animal animal, boolean hasMedicalRecord, Authentication authentication) {
        model.addAttribute("animal", animal);
        model.addAttribute("hasMedicalRecord", hasMedicalRecord);
        model.addAttribute("formAction", animal.getId() == null ? "/animals/create" : "/animals/edit/" + animal.getId());
        
        Client matchedClient = null;
        if (authentication != null) {
            matchedClient = clientService.getAllClients(Pageable.unpaged()).getContent().stream()
                    .filter(c -> c.getEmail() != null && accessControlService.matchesUsername(c.getEmail(), authentication.getName()))
                    .findFirst().orElse(null);
        }
        model.addAttribute("matchedClient", matchedClient);
        
        populateFormOptions(model);
        return "animals/form";
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

    private Sort buildSort(String sort, String dir) {
        String property = switch (sort) {
            case "species" -> "species";
            case "age" -> "age";
            default -> "name";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property).and(Sort.by(Sort.Direction.ASC, "name"));
    }
}
