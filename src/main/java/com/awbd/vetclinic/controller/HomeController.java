package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.service.AnimalService;
import com.awbd.vetclinic.service.AccessControlService;
import com.awbd.vetclinic.service.AppointmentService;
import com.awbd.vetclinic.service.ClientService;
import com.awbd.vetclinic.service.DoctorService;
import com.awbd.vetclinic.service.MedicalRecordService;
import com.awbd.vetclinic.service.SpecialtyService;
import com.awbd.vetclinic.service.TreatmentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AnimalService animalService;
    private final AccessControlService accessControlService;
    private final ClientService clientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final SpecialtyService specialtyService;
    private final TreatmentService treatmentService;

    public HomeController(AnimalService animalService,
                          AccessControlService accessControlService,
                          ClientService clientService,
                          DoctorService doctorService,
                          AppointmentService appointmentService,
                          MedicalRecordService medicalRecordService,
                          SpecialtyService specialtyService,
                          TreatmentService treatmentService) {
        this.animalService = animalService;
        this.accessControlService = accessControlService;
        this.clientService = clientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
        this.specialtyService = specialtyService;
        this.treatmentService = treatmentService;
    }

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        var animalsPage = animalService.getAllAnimals(Pageable.unpaged());
        var appointmentsPage = appointmentService.getAllAppointments(Pageable.unpaged());
        boolean userPortal = accessControlService.isUser(authentication)
                && !accessControlService.isEmployee(authentication)
                && !accessControlService.isAdmin(authentication);

        model.addAttribute("animalCount", animalsPage.getTotalElements());
        model.addAttribute("clientCount", clientService.getAllClients(Pageable.unpaged()).getTotalElements());
        model.addAttribute("doctorCount", doctorService.getAllDoctors(Pageable.unpaged()).getTotalElements());
        model.addAttribute("appointmentCount", appointmentsPage.getTotalElements());
        model.addAttribute("medicalRecordCount", medicalRecordService.getAllMedicalRecords(Pageable.unpaged()).getTotalElements());
        model.addAttribute("specialtyCount", specialtyService.getAllSpecialties(Pageable.unpaged()).getTotalElements());
        model.addAttribute("treatmentCount", treatmentService.getAllTreatments(Pageable.unpaged()).getTotalElements());
        model.addAttribute("visibleAnimalCount", accessControlService.filterAnimals(animalsPage, authentication).getTotalElements());
        model.addAttribute("visibleAppointmentCount", accessControlService.filterAppointments(appointmentsPage, authentication).getTotalElements());
        model.addAttribute("userPortal", userPortal);
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
