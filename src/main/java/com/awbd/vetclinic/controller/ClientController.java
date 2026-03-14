package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.service.ClientService;
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
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public String listClients(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show clients page: {}", page);
        Page<Client> clientPage = clientService.getAllClients(PageRequest.of(page, 5));

        model.addAttribute("clientPage", clientPage);
        model.addAttribute("currentPage", page);
        return "clients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("client", new Client());
        return "clients/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("client") Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for client creation");
            return "clients/form";
        }
        clientService.create(client);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Client client = clientService.getClientById(id);
        model.addAttribute("client", client);
        return "clients/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }
}

