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
        return showForm(model, new Specialty());
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("specialty") Specialty specialty, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for specialty creation");
            return showForm(model, specialty);
        }
        specialtyService.create(specialty);
        return "redirect:/specialties";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showForm(model, specialtyService.getSpecialtyById(id));
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("specialty") Specialty specialty,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for specialty update with id: {}", id);
            specialty.setId(id);
            return showForm(model, specialty);
        }
        specialtyService.update(id, specialty);
        return "redirect:/specialties";
    }

    @GetMapping("/delete/{id}")
    public String deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return "redirect:/specialties";
    }

    private String showForm(Model model, Specialty specialty) {
        model.addAttribute("specialty", specialty);
        model.addAttribute("formAction", specialty.getId() == null ? "/specialties/create" : "/specialties/edit/" + specialty.getId());
        return "specialties/form";
    }
}

