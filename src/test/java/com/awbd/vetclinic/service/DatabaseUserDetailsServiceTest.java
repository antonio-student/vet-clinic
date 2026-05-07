package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.AppUser;
import com.awbd.vetclinic.model.UserRole;
import com.awbd.vetclinic.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private DatabaseUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new DatabaseUserDetailsService(appUserRepository);
    }

    @Test
    @DisplayName("Should load user by username when user exists")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        AppUser appUser = new AppUser();
        appUser.setUsername("testuser");
        appUser.setPassword("password");
        appUser.setEnabled(true);
        appUser.setRoles(Set.of(UserRole.ROLE_ADMIN));

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(appUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown")
        );
    }
}
