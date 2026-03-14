package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.MedicalRecord;
import com.awbd.vetclinic.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
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
        model.addAttribute("medicalRecord", new MedicalRecord());
        return "medical-records/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for medical record creation");
            return "medical-records/form";
        }
        medicalRecordService.create(medicalRecord);
        return "redirect:/medical-records";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id);
        model.addAttribute("medicalRecord", medicalRecord);
        return "medical-records/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return "redirect:/medical-records";
    }
}

