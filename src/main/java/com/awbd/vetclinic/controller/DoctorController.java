package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.service.DoctorService;
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
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
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
        return "doctors/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("doctor") Doctor doctor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for doctor creation");
            return "doctors/form";
        }
        doctorService.create(doctor);
        return "redirect:/doctors";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        return "doctors/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "redirect:/doctors";
    }
}

