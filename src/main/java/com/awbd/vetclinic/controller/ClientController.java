package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.service.ClientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public String listClients(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String name,
                              @RequestParam(defaultValue = "") String email,
                              Model model) {
        log.info("Request to show clients page: {}, name: {}, email: {}", page, name, email);
        Page<Client> clientPage = clientService.searchClients(name, email, PageRequest.of(page, 5, Sort.by("name").ascending()));

        model.addAttribute("clientPage", clientPage);
        addListAttributes(model, page, name, email);
        return "clients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return showForm(model, new Client());
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("client") Client client, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for client creation");
            return showForm(model, client);
        }
        clientService.create(client);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showForm(model, clientService.getClientById(id));
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("client") Client client,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for client update with id: {}", id);
            client.setId(id);
            return showForm(model, client);
        }
        clientService.update(id, client);
        return "redirect:/clients";
    }

    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }

    private String showForm(Model model, Client client) {
        model.addAttribute("client", client);
        model.addAttribute("formAction", client.getId() == null ? "/clients/create" : "/clients/edit/" + client.getId());
        return "clients/form";
    }

    private void addListAttributes(Model model, int page, String name, String email) {
        model.addAttribute("currentPage", page);
        model.addAttribute("nameFilter", name);
        model.addAttribute("emailFilter", email);
    }
}

