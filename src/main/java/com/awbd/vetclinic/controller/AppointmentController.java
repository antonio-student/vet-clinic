package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.service.AppointmentService;
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
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String listAppointments(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("Request to show appointments page: {}", page);
        Page<Appointment> appointmentPage = appointmentService.getAllAppointments(PageRequest.of(page, 5));

        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("currentPage", page);
        return "appointments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        return "appointments/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("appointment") Appointment appointment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for appointment creation");
            return "appointments/form";
        }
        appointmentService.create(appointment);
        return "redirect:/appointments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        model.addAttribute("appointment", appointment);
        return "appointments/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/appointments";
    }
}

