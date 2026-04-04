package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.service.AnimalService;
import com.awbd.vetclinic.service.AppointmentService;
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

@Slf4j
@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final AnimalService animalService;

    public AppointmentController(AppointmentService appointmentService, DoctorService doctorService, AnimalService animalService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.animalService = animalService;
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
        Appointment appointment = new Appointment();
        appointment.setDoctor(new Doctor());
        appointment.setAnimal(new Animal());
        model.addAttribute("appointment", appointment);
        populateFormOptions(model);
        return "appointments/form";
    }

    @PostMapping("/save")
    public String create(@Valid @ModelAttribute("appointment") Appointment appointment, BindingResult bindingResult, Model model) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            bindingResult.rejectValue("doctor", "appointment.doctor", "Doctor is required");
        }
        if (appointment.getAnimal() == null || appointment.getAnimal().getId() == null) {
            bindingResult.rejectValue("animal", "appointment.animal", "Patient is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for appointment creation");
            if (appointment.getDoctor() == null) {
                appointment.setDoctor(new Doctor());
            }
            if (appointment.getAnimal() == null) {
                appointment.setAnimal(new Animal());
            }
            populateFormOptions(model);
            return "appointments/form";
        }
        appointment.setDoctor(doctorService.getDoctorById(appointment.getDoctor().getId()));
        appointment.setAnimal(animalService.getAnimalById(appointment.getAnimal().getId()));
        appointmentService.create(appointment);
        return "redirect:/appointments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        model.addAttribute("appointment", appointment);
        populateFormOptions(model);
        return "appointments/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/appointments";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors(Pageable.unpaged()).getContent());
        model.addAttribute("animals", animalService.getAllAnimals(Pageable.unpaged()).getContent());
    }
}

