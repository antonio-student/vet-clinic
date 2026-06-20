package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.repository.AnimalRepository;
import com.awbd.vetclinic.repository.AppointmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AccessControlService {

    private final AnimalRepository animalRepository;
    private final AppointmentRepository appointmentRepository;

    public AccessControlService(AnimalRepository animalRepository, AppointmentRepository appointmentRepository) {
        this.animalRepository = animalRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    public boolean isEmployee(Authentication authentication) {
        return hasRole(authentication, "ROLE_EMPLOYEE");
    }

    public boolean isUser(Authentication authentication) {
        return hasRole(authentication, "ROLE_USER");
    }

    public boolean canAccessAnimal(Long animalId, Authentication authentication) {
        if (animalId == null || authentication == null) {
            return false;
        }
        if (isEmployee(authentication) || isAdmin(authentication)) {
            return true;
        }
        return animalRepository.findById(animalId)
                .map(animal -> isOwnedByUsername(animal, authentication.getName()))
                .orElse(false);
    }

    public boolean canUseAnimalForWrite(Long animalId, Authentication authentication) {
        return canAccessAnimal(animalId, authentication);
    }

    public Page<Animal> filterAnimals(Page<Animal> animalPage, Authentication authentication) {
        if (authentication == null || isEmployee(authentication) || isAdmin(authentication)) {
            return animalPage;
        }

        List<Animal> ownedAnimals = animalPage.getContent().stream()
                .filter(animal -> isOwnedByUsername(animal, authentication.getName()))
                .toList();

        return new PageImpl<>(ownedAnimals, animalPage.getPageable(), ownedAnimals.size());
    }

    public Page<Appointment> filterAppointments(Page<Appointment> appointmentPage, Authentication authentication) {
        if (authentication == null || isEmployee(authentication) || isAdmin(authentication)) {
            return appointmentPage;
        }

        List<Appointment> ownedAppointments = appointmentPage.getContent().stream()
                .filter(appointment -> appointment.getAnimal() != null && isOwnedByUsername(appointment.getAnimal(), authentication.getName()))
                .toList();

        return new PageImpl<>(ownedAppointments, appointmentPage.getPageable(), ownedAppointments.size());
    }

    public boolean canAccessAppointment(Long appointmentId, Authentication authentication) {
        if (appointmentId == null || authentication == null) {
            return false;
        }
        if (isEmployee(authentication) || isAdmin(authentication)) {
            return true;
        }
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> appointment.getAnimal() != null && isOwnedByUsername(appointment.getAnimal(), authentication.getName()))
                .orElse(false);
    }

    private boolean isOwnedByUsername(Animal animal, String username) {
        return animal.getClient() != null
                && animal.getClient().getEmail() != null
                && matchesUsername(animal.getClient().getEmail(), username);
    }

    public boolean matchesUsername(String email, String username) {
        if (email == null || username == null || username.isBlank()) {
            return false;
        }
        if (email.equalsIgnoreCase(username)) {
            return true;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 && email.substring(0, atIndex).equalsIgnoreCase(username);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> Objects.equals(grantedAuthority.getAuthority(), role));
    }
}
