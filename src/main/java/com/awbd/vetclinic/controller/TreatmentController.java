package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Treatment;
import com.awbd.vetclinic.model.MedicalRecord;
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
@RequestMapping("/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final MedicalRecordService medicalRecordService;

    public TreatmentController(TreatmentService treatmentService, MedicalRecordService medicalRecordService) {
        this.treatmentService = treatmentService;
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping
    public String listTreatments(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show treatments page: {}", page);
        Page<Treatment> treatmentPage = treatmentService.getAllTreatments(PageRequest.of(page, 5));

        model.addAttribute("treatmentPage", treatmentPage);
        model.addAttribute("currentPage", page);
        return "treatments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Treatment treatment = new Treatment();
        treatment.setMedicalRecord(new MedicalRecord());
        model.addAttribute("treatment", treatment);
        populateFormOptions(model);
        return "treatments/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("treatment") Treatment treatment, BindingResult bindingResult, Model model) {
        if (treatment.getMedicalRecord() == null || treatment.getMedicalRecord().getId() == null) {
            bindingResult.rejectValue("medicalRecord", "treatment.medicalRecord", "Medical record is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for treatment creation");
            if (treatment.getMedicalRecord() == null) {
                treatment.setMedicalRecord(new MedicalRecord());
            }
            populateFormOptions(model);
            return "treatments/form";
        }
        treatment.setMedicalRecord(medicalRecordService.getMedicalRecordById(treatment.getMedicalRecord().getId()));
        treatmentService.create(treatment);
        return "redirect:/treatments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Treatment treatment = treatmentService.getTreatmentById(id);
        model.addAttribute("treatment", treatment);
        populateFormOptions(model);
        return "treatments/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable Long id) {
        treatmentService.deleteTreatment(id);
        return "redirect:/treatments";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("medicalRecords", medicalRecordService.getAllMedicalRecords(Pageable.unpaged()).getContent());
    }
}

