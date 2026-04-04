package com.awbd.vetclinic.config;

import com.awbd.vetclinic.model.AppUser;
import com.awbd.vetclinic.model.UserRole;
import com.awbd.vetclinic.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class SecurityDataInitializer {

    @Bean
    CommandLineRunner seedUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (appUserRepository.count() > 0) {
                return;
            }

            appUserRepository.save(buildUser("admin", "admin123", Set.of(UserRole.ROLE_ADMIN, UserRole.ROLE_EMPLOYEE, UserRole.ROLE_USER), passwordEncoder));
            appUserRepository.save(buildUser("employee", "employee123", Set.of(UserRole.ROLE_EMPLOYEE), passwordEncoder));
            appUserRepository.save(buildUser("user", "user123", Set.of(UserRole.ROLE_USER), passwordEncoder));
        };
    }

    private AppUser buildUser(String username, String rawPassword, Set<UserRole> roles, PasswordEncoder passwordEncoder) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        appUser.setEnabled(true);
        appUser.setRoles(roles);
        return appUser;
    }
}
