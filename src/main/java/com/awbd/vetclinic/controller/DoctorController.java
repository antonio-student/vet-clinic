package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.model.Specialty;
import com.awbd.vetclinic.service.DoctorService;
import com.awbd.vetclinic.service.SpecialtyService;
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
    public String listDoctors(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String name,
                              @RequestParam(defaultValue = "") String email,
                              Model model) {
        log.info("Request to show doctors page: {}, name: {}, email: {}", page, name, email);
        Page<Doctor> doctorPage = doctorService.searchDoctors(name, email, PageRequest.of(page, 5, Sort.by("lastName", "firstName").ascending()));

        model.addAttribute("doctorPage", doctorPage);
        addListAttributes(model, page, name, email);
        return "doctors/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showForm(model, new Doctor(), List.of(), "");
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("doctor") Doctor doctor,
                         BindingResult bindingResult,
                         @RequestParam(name = "specialtyIds", required = false) List<Long> specialtyIds,
                         @RequestParam(name = "newSpecialties", required = false) String newSpecialties,
                         Model model) {
        doctor.setSpecialties(resolveSpecialties(specialtyIds, newSpecialties));
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for doctor creation");
            return showForm(model, doctor, specialtyIds, newSpecialties);
        }
        doctorService.create(doctor);
        return "redirect:/doctors";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        return showForm(model, doctor, doctor.getSpecialties().stream().map(Specialty::getId).toList(), "");
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("doctor") Doctor doctor,
                         BindingResult bindingResult,
                         @RequestParam(name = "specialtyIds", required = false) List<Long> specialtyIds,
                         @RequestParam(name = "newSpecialties", required = false) String newSpecialties,
                         Model model) {
        doctor.setSpecialties(resolveSpecialties(specialtyIds, newSpecialties));
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for doctor update with id: {}", id);
            doctor.setId(id);
            return showForm(model, doctor, specialtyIds, newSpecialties);
        }
        doctorService.update(id, doctor);
        return "redirect:/doctors";
    }

    @GetMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "redirect:/doctors";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("specialties", specialtyService.getAllSpecialties(Pageable.unpaged()).getContent());
    }

    private String showForm(Model model, Doctor doctor, List<Long> specialtyIds, String newSpecialties) {
        model.addAttribute("doctor", doctor);
        model.addAttribute("formAction", doctor.getId() == null ? "/doctors/create" : "/doctors/edit/" + doctor.getId());
        model.addAttribute("selectedSpecialtyIds", specialtyIds == null ? List.of() : specialtyIds);
        model.addAttribute("newSpecialties", newSpecialties == null ? "" : newSpecialties);
        populateFormOptions(model);
        return "doctors/form";
    }

    private void addListAttributes(Model model, int page, String name, String email) {
        model.addAttribute("currentPage", page);
        model.addAttribute("nameFilter", name);
        model.addAttribute("emailFilter", email);
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

