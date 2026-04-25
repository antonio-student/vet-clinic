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
import org.springframework.data.domain.Sort;
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
    public String listTreatments(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "treatmentDate") String sort,
                                 @RequestParam(defaultValue = "desc") String dir,
                                 Model model) {
        log.info("Request to show treatments page: {}, sort: {}, dir: {}", page, sort, dir);
        Page<Treatment> treatmentPage = treatmentService.getAllTreatments(PageRequest.of(page, 5, buildSort(sort, dir)));

        model.addAttribute("treatmentPage", treatmentPage);
        addListAttributes(model, page, sort, dir);
        return "treatments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Treatment treatment = new Treatment();
        treatment.setMedicalRecord(new MedicalRecord());
        return showForm(model, treatment);
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("treatment") Treatment treatment, BindingResult bindingResult, Model model) {
        if (treatment.getMedicalRecord() == null || treatment.getMedicalRecord().getId() == null) {
            bindingResult.rejectValue("medicalRecord", "treatment.medicalRecord", "Medical record is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for treatment creation");
            if (treatment.getMedicalRecord() == null) {
                treatment.setMedicalRecord(new MedicalRecord());
            }
            return showForm(model, treatment);
        }
        treatment.setMedicalRecord(medicalRecordService.getMedicalRecordById(treatment.getMedicalRecord().getId()));
        treatmentService.create(treatment);
        return "redirect:/treatments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showForm(model, treatmentService.getTreatmentById(id));
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("treatment") Treatment treatment,
                         BindingResult bindingResult,
                         Model model) {
        if (treatment.getMedicalRecord() == null || treatment.getMedicalRecord().getId() == null) {
            bindingResult.rejectValue("medicalRecord", "treatment.medicalRecord", "Medical record is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for treatment update with id: {}", id);
            treatment.setId(id);
            if (treatment.getMedicalRecord() == null) {
                treatment.setMedicalRecord(new MedicalRecord());
            }
            return showForm(model, treatment);
        }
        treatment.setMedicalRecord(medicalRecordService.getMedicalRecordById(treatment.getMedicalRecord().getId()));
        treatmentService.update(id, treatment);
        return "redirect:/treatments";
    }

    @GetMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable Long id) {
        treatmentService.deleteTreatment(id);
        return "redirect:/treatments";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("medicalRecords", medicalRecordService.getAllMedicalRecords(Pageable.unpaged()).getContent());
    }

    private String showForm(Model model, Treatment treatment) {
        model.addAttribute("treatment", treatment);
        model.addAttribute("formAction", treatment.getId() == null ? "/treatments/create" : "/treatments/edit/" + treatment.getId());
        populateFormOptions(model);
        return "treatments/form";
    }

    private void addListAttributes(Model model, int page, String sort, String dir) {
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
    }

    private Sort buildSort(String sort, String dir) {
        String property = switch (sort) {
            case "cost" -> "cost";
            case "treatmentDate" -> "treatmentDate";
            default -> "treatmentDate";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}

