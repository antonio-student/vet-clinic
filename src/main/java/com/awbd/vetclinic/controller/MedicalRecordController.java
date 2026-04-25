package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.MedicalRecord;
import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.service.AnimalService;
import com.awbd.vetclinic.service.MedicalRecordService;
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
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final AnimalService animalService;

    public MedicalRecordController(MedicalRecordService medicalRecordService, AnimalService animalService) {
        this.medicalRecordService = medicalRecordService;
        this.animalService = animalService;
    }

    @GetMapping
    public String listMedicalRecords(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show medical records page: {}", page);
        Page<MedicalRecord> medicalRecordPage = medicalRecordService.getAllMedicalRecords(PageRequest.of(page, 5));

        model.addAttribute("medicalRecordPage", medicalRecordPage);
        model.addAttribute("currentPage", page);
        return "medical-records/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setAnimal(new Animal());
        return showForm(model, medicalRecord);
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
                         BindingResult bindingResult,
                         Model model) {
        if (medicalRecord.getAnimal() == null || medicalRecord.getAnimal().getId() == null) {
            bindingResult.rejectValue("animal", "medicalRecord.animal", "Patient is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for medical record creation");
            if (medicalRecord.getAnimal() == null) {
                medicalRecord.setAnimal(new Animal());
            }
            return showForm(model, medicalRecord);
        }
        medicalRecord.setAnimal(animalService.getAnimalById(medicalRecord.getAnimal().getId()));
        medicalRecordService.create(medicalRecord);
        return "redirect:/medical-records";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showForm(model, medicalRecordService.getMedicalRecordById(id));
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
                         BindingResult bindingResult,
                         Model model) {
        if (medicalRecord.getAnimal() == null || medicalRecord.getAnimal().getId() == null) {
            bindingResult.rejectValue("animal", "medicalRecord.animal", "Patient is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for medical record update with id: {}", id);
            medicalRecord.setId(id);
            if (medicalRecord.getAnimal() == null) {
                medicalRecord.setAnimal(new Animal());
            }
            return showForm(model, medicalRecord);
        }
        medicalRecord.setAnimal(animalService.getAnimalById(medicalRecord.getAnimal().getId()));
        medicalRecordService.update(id, medicalRecord);
        return "redirect:/medical-records";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return "redirect:/medical-records";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("animals", animalService.getAllAnimals(Pageable.unpaged()).getContent());
    }

    private String showForm(Model model, MedicalRecord medicalRecord) {
        model.addAttribute("medicalRecord", medicalRecord);
        model.addAttribute("formAction", medicalRecord.getId() == null ? "/medical-records/create" : "/medical-records/edit/" + medicalRecord.getId());
        populateFormOptions(model);
        return "medical-records/form";
    }
}

