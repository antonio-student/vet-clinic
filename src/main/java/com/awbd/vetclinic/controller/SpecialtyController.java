package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Specialty;
import com.awbd.vetclinic.service.SpecialtyService;
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
@RequestMapping("/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public String listSpecialties(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show specialties page: {}", page);
        Page<Specialty> specialtyPage = specialtyService.getAllSpecialties(PageRequest.of(page, 5));

        model.addAttribute("specialtyPage", specialtyPage);
        model.addAttribute("currentPage", page);
        return "specialties/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("specialty", new Specialty());
        return "specialties/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("specialty") Specialty specialty, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for specialty creation");
            return "specialties/form";
        }
        specialtyService.create(specialty);
        return "redirect:/specialties";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Specialty specialty = specialtyService.getSpecialtyById(id);
        model.addAttribute("specialty", specialty);
        return "specialties/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return "redirect:/specialties";
    }
}

