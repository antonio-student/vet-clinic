package com.awbd.vetclinic.config;

import com.awbd.vetclinic.repository.ClientRepository;
import com.awbd.vetclinic.service.AccessControlService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OwnerAccessFilter extends OncePerRequestFilter {

    private final AccessControlService accessControlService;
    private final ClientRepository clientRepository;

    public OwnerAccessFilter(AccessControlService accessControlService, ClientRepository clientRepository) {
        this.accessControlService = accessControlService;
        this.clientRepository = clientRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String path = request.getServletPath();

        if (authentication == null
                || !authentication.isAuthenticated()
                || accessControlService.isEmployee(authentication)
                || accessControlService.isAdmin(authentication)
                || !accessControlService.isUser(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAnimalProfileRequest(request, path) && !canAccessAnimalPath(path, authentication)) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }

        if (isAnimalCreateRequest(request, path) && !canCreateAnimal(request, authentication)) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }

        if (isAppointmentSaveRequest(request, path) && !canUseAppointmentAnimal(request, authentication)) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAnimalProfileRequest(HttpServletRequest request, String path) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && path.matches("^/animals/\\d+$");
    }

    private boolean isAnimalCreateRequest(HttpServletRequest request, String path) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/animals/save".equals(path);
    }

    private boolean isAppointmentSaveRequest(HttpServletRequest request, String path) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/appointments/save".equals(path);
    }

    private boolean canAccessAnimalPath(String path, Authentication authentication) {
        Long animalId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
        return accessControlService.canAccessAnimal(animalId, authentication);
    }

    private boolean canCreateAnimal(HttpServletRequest request, Authentication authentication) {
        String clientIdRaw = request.getParameter("client.id");
        if (clientIdRaw == null || clientIdRaw.isBlank()) {
            return false;
        }

        Long clientId = Long.parseLong(clientIdRaw);
        return clientRepository.findById(clientId)
                .map(client -> client.getEmail() != null && client.getEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    private boolean canUseAppointmentAnimal(HttpServletRequest request, Authentication authentication) {
        String animalIdRaw = request.getParameter("animal.id");
        if (animalIdRaw == null || animalIdRaw.isBlank()) {
            return false;
        }

        return accessControlService.canUseAnimalForWrite(Long.parseLong(animalIdRaw), authentication);
    }
}
