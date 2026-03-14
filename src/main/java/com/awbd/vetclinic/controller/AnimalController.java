package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.service.AnimalService;
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
@RequestMapping("/animals")
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public String listAnimals(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show animals page: {}", page);
        // Page size is set to 5 for demonstration
        Page<Animal> animalPage = animalService.getAllAnimals(PageRequest.of(page, 5));

        model.addAttribute("animalPage", animalPage);
        model.addAttribute("currentPage", page);
        return "animals/list"; // This matches the Thymeleaf template path
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("animal", new Animal());
        return "animals/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("animal") Animal animal, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for animal creation");
            return "animals/form"; // Return to form to show validation messages
        }
        animalService.create(animal);
        return "redirect:/animals";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Animal animal = animalService.getAnimalById(id);
        model.addAttribute("animal", animal);
        return "animals/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return "redirect:/animals";
    }
}
