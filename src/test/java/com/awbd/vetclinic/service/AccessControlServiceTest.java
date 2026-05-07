package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Animal;
import com.awbd.vetclinic.model.Appointment;
import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.repository.AnimalRepository;
import com.awbd.vetclinic.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private Authentication authentication;

    private AccessControlService accessControlService;

    @BeforeEach
    void setUp() {
        accessControlService = new AccessControlService(animalRepository, appointmentRepository);
    }

    @Test
    @DisplayName("isAdmin should return true for ADMIN role")
    void isAdmin_WithAdminRole_ShouldReturnTrue() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        assertTrue(accessControlService.isAdmin(authentication));
    }

    @Test
    @DisplayName("isEmployee should return true for EMPLOYEE role")
    void isEmployee_WithEmployeeRole_ShouldReturnTrue() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(authentication).getAuthorities();
        assertTrue(accessControlService.isEmployee(authentication));
    }

    @Test
    @DisplayName("isUser should return true for USER role")
    void isUser_WithUserRole_ShouldReturnTrue() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        assertTrue(accessControlService.isUser(authentication));
    }

    @Test
    @DisplayName("canAccessAnimal should return true for ADMIN/EMPLOYEE")
    void canAccessAnimal_WithAdmin_ShouldReturnTrue() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        assertTrue(accessControlService.canAccessAnimal(1L, authentication));
    }

    @Test
    @DisplayName("canAccessAnimal should return true if user owns the animal")
    void canAccessAnimal_WithUserOwner_ShouldReturnTrue() {
        Long animalId = 1L;
        Animal animal = new Animal();
        Client client = new Client();
        client.setEmail("user@test.com");
        animal.setClient(client);

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("user@test.com");
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        assertTrue(accessControlService.canAccessAnimal(animalId, authentication));
    }

    @Test
    @DisplayName("matchesUsername should handle variations")
    void matchesUsername_ShouldHandleCases() {
        assertTrue(accessControlService.matchesUsername("user@test.com", "user@test.com"));
        assertTrue(accessControlService.matchesUsername("user@test.com", "user"));
        assertFalse(accessControlService.matchesUsername("user@test.com", "other"));
        assertFalse(accessControlService.matchesUsername(null, "user"));
        assertFalse(accessControlService.matchesUsername("user@test.com", null));
        assertFalse(accessControlService.matchesUsername("user@test.com", "  "));
    }

    @Test
    @DisplayName("filterAnimals should return all for admin")
    void filterAnimals_WithAdmin_ShouldReturnAll() {
        Page<Animal> page = new PageImpl<>(List.of(new Animal()));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        Page<Animal> result = accessControlService.filterAnimals(page, authentication);

        assertEquals(page, result);
    }

    @Test
    @DisplayName("filterAnimals should filter for user")
    void filterAnimals_WithUser_ShouldFilter() {
        Animal myAnimal = new Animal();
        Client me = new Client();
        me.setEmail("me@test.com");
        myAnimal.setClient(me);

        Animal otherAnimal = new Animal();
        Client other = new Client();
        other.setEmail("other@test.com");
        otherAnimal.setClient(other);

        Page<Animal> page = new PageImpl<>(List.of(myAnimal, otherAnimal));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("me@test.com");

        Page<Animal> result = accessControlService.filterAnimals(page, authentication);

        assertEquals(1, result.getContent().size());
        assertEquals("me@test.com", result.getContent().getFirst().getClient().getEmail());
    }

    @Test
    @DisplayName("canAccessAppointment should return true for owner")
    void canAccessAppointment_WithOwner_ShouldReturnTrue() {
        Long appId = 1L;
        Appointment app = new Appointment();
        Animal animal = new Animal();
        Client client = new Client();
        client.setEmail("owner@test.com");
        animal.setClient(client);
        app.setAnimal(animal);

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("owner@test.com");
        when(appointmentRepository.findById(appId)).thenReturn(Optional.of(app));

        assertTrue(accessControlService.canAccessAppointment(appId, authentication));
    }
}
