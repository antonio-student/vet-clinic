package com.awbd.vetclinic.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, OwnerAccessFilter ownerAccessFilter) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/animals-ui.css", "/logo.svg", "/logo.png", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/access-denied").authenticated()
                        .requestMatchers("/doctors/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/doctors").authenticated()
                        .requestMatchers(HttpMethod.GET, "/doctors/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/doctors/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/specialties/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/specialties").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/specialties/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/treatments/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/treatments").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/treatments/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/medical-records/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/medical-records/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/clients/delete/**").hasRole("ADMIN")
                        .requestMatchers("/clients/edit/**", "/clients/save", "/clients/new").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/clients/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/animals/delete/**").hasRole("ADMIN")
                        .requestMatchers("/animals/edit/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/animals/*/medical-record", "/animals/*/treatments").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/animals/**").authenticated()
                        .requestMatchers("/appointments/delete/**").hasRole("ADMIN")
                        .requestMatchers("/appointments/edit/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/appointments/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("buddy-care-remember-me-key")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/access-denied")
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .addFilterAfter(ownerAccessFilter, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
