package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.service.SpecialtyService;
import com.awbd.vetclinic.service.DoctorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;

    public DoctorController(DoctorService doctorService, SpecialtyService specialtyService) {
        this.doctorService = doctorService;
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public String listDoctors(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show doctors page: {}", page);
        Page<Doctor> doctorPage = doctorService.getAllDoctors(PageRequest.of(page, 5));

        model.addAttribute("doctorPage", doctorPage);
        model.addAttribute("currentPage", page);
        return "doctors/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        populateFormOptions(model);
        model.addAttribute("selectedSpecialtyIds", List.of());
        model.addAttribute("newSpecialties", "");
        return "doctors/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("doctor") Doctor doctor,
                         BindingResult bindingResult,
                         @RequestParam(name = "specialtyIds", required = false) List<Long> specialtyIds,
                         @RequestParam(name = "newSpecialties", required = false) String newSpecialties,
                         Model model) {
        doctor.setSpecialties(resolveSpecialties(specialtyIds, newSpecialties));
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for doctor creation");
            populateFormOptions(model);
            model.addAttribute("selectedSpecialtyIds", specialtyIds == null ? List.of() : specialtyIds);
            model.addAttribute("newSpecialties", newSpecialties == null ? "" : newSpecialties);
            return "doctors/form";
        }
        doctorService.create(doctor);
        return "redirect:/doctors";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        populateFormOptions(model);
        model.addAttribute("selectedSpecialtyIds", doctor.getSpecialties().stream().map(specialty -> specialty.getId()).toList());
        model.addAttribute("newSpecialties", "");
        return "doctors/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "redirect:/doctors";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("specialties", specialtyService.getAllSpecialties(Pageable.unpaged()).getContent());
    }

    private List<com.awbd.vetclinic.model.Specialty> resolveSpecialties(List<Long> specialtyIds, String newSpecialties) {
        Map<Long, com.awbd.vetclinic.model.Specialty> specialties = new LinkedHashMap<>();

        if (specialtyIds != null) {
            for (Long specialtyId : specialtyIds) {
                com.awbd.vetclinic.model.Specialty specialty = specialtyService.getSpecialtyById(specialtyId);
                specialties.put(specialty.getId(), specialty);
            }
        }

        if (newSpecialties != null && !newSpecialties.isBlank()) {
            String[] specialtyNames = newSpecialties.split(",");
            for (String specialtyName : specialtyNames) {
                String normalizedName = specialtyName.trim();
                if (!normalizedName.isEmpty()) {
                    com.awbd.vetclinic.model.Specialty specialty = specialtyService.getOrCreateByName(normalizedName);
                    specialties.put(specialty.getId(), specialty);
                }
            }
        }

        return new ArrayList<>(specialties.values());
    }
}

