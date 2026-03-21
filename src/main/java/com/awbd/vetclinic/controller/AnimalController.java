package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.service.AnimalService;
import com.awbd.vetclinic.service.ClientService;
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
@RequestMapping("/animals")
public class AnimalController {
    private final AnimalService animalService;
    private final ClientService clientService;

    public AnimalController(AnimalService animalService, ClientService clientService) {
        this.animalService = animalService;
        this.clientService = clientService;
    }

    @GetMapping
    public String listAnimals(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show animals page: {}", page);
        Page<Animal> animalPage = animalService.getAllAnimals(PageRequest.of(page, 7));

        model.addAttribute("animalPage", animalPage);
        model.addAttribute("currentPage", page);
        return "animals/list"; // This matches the Thymeleaf template path
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Animal animal = new Animal();
        animal.setClient(new Client());
        model.addAttribute("animal", animal);
        populateFormOptions(model);
        return "animals/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("animal") Animal animal, BindingResult bindingResult, Model model) {
        if (animal.getClient() == null || animal.getClient().getId() == null) {
            bindingResult.rejectValue("client", "animal.client", "Owner is required");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for animal creation");
            if (animal.getClient() == null) {
                animal.setClient(new Client());
            }
            populateFormOptions(model);
            return "animals/form"; // Return to form to show validation messages
        }

        animal.setClient(clientService.getClientById(animal.getClient().getId()));
        animalService.create(animal);
        return "redirect:/animals";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Animal animal = animalService.getAnimalById(id);
        model.addAttribute("animal", animal);
        populateFormOptions(model);
        return "animals/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return "redirect:/animals";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("clients", clientService.getAllClients(Pageable.unpaged()).getContent());
    }
}
