package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.model.Doctor;
import com.awbd.vetclinic.service.AccessControlService;
import com.awbd.vetclinic.service.AnimalService;
import com.awbd.vetclinic.service.AppointmentService;
import com.awbd.vetclinic.service.DoctorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final AnimalService animalService;
    private final AccessControlService accessControlService;

    public AppointmentController(AppointmentService appointmentService,
                                 DoctorService doctorService,
                                 AnimalService animalService,
                                 AccessControlService accessControlService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.animalService = animalService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public String listAppointments(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(required = false) Long animalId,
                                   @RequestParam(required = false) LocalDate appointmentDate,
                                   Model model,
                                   Authentication authentication) {
        log.info("Request to show appointments page: {}, animalId: {}, appointmentDate: {}", page, animalId, appointmentDate);
        Page<Appointment> appointmentPage = appointmentService.searchAppointments(
                animalId,
                appointmentDate,
                PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "appointmentDate"))
        );
        appointmentPage = accessControlService.filterAppointments(appointmentPage, authentication);

        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedAnimalId", animalId);
        model.addAttribute("appointmentDateFilter", appointmentDate == null ? "" : appointmentDate.toString());
        Page<Animal> availableAnimals = animalService.getAllAnimals(Pageable.unpaged());
        model.addAttribute("filterAnimals", accessControlService.filterAnimals(availableAnimals, authentication).getContent());
        return "appointments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Appointment appointment = new Appointment();
        appointment.setDoctor(new Doctor());
        appointment.setAnimal(new Animal());
        appointment.setStatus("SCHEDULED");
        return showForm(model, appointment);
    }

    @PostMapping("/create")
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
            return showForm(model, appointment);
        }
        appointment.setDoctor(doctorService.getDoctorById(appointment.getDoctor().getId()));
        appointment.setAnimal(animalService.getAnimalById(appointment.getAnimal().getId()));
        appointmentService.create(appointment);
        return "redirect:/appointments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return showForm(model, appointmentService.getAppointmentById(id));
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("appointment") Appointment appointment,
                         BindingResult bindingResult,
                         Model model) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            bindingResult.rejectValue("doctor", "appointment.doctor", "Doctor is required");
        }
        if (appointment.getAnimal() == null || appointment.getAnimal().getId() == null) {
            bindingResult.rejectValue("animal", "appointment.animal", "Patient is required");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for appointment update with id: {}", id);
            appointment.setId(id);
            if (appointment.getDoctor() == null) {
                appointment.setDoctor(new Doctor());
            }
            if (appointment.getAnimal() == null) {
                appointment.setAnimal(new Animal());
            }
            return showForm(model, appointment);
        }
        appointment.setDoctor(doctorService.getDoctorById(appointment.getDoctor().getId()));
        appointment.setAnimal(animalService.getAnimalById(appointment.getAnimal().getId()));
        appointmentService.update(id, appointment);
        return "redirect:/appointments";
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

    private String showForm(Model model, Appointment appointment) {
        model.addAttribute("appointment", appointment);
        model.addAttribute("formAction", appointment.getId() == null ? "/appointments/create" : "/appointments/edit/" + appointment.getId());
        model.addAttribute("selectedAnimalId", appointment.getAnimal() != null ? appointment.getAnimal().getId() : null);
        model.addAttribute("selectedDoctorId", appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);
        model.addAttribute("appointmentDateValue", appointment.getAppointmentDate() == null
                ? ""
                : appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        populateFormOptions(model);
        return "appointments/form";
    }
}
