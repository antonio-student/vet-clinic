package com.awbd.vetclinic.controller;

import com.awbd.vetclinic.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception exception, HttpServletRequest request, Model model) {
        log.warn("Resource not found on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        populateErrorModel(model, "Page not found",
                "The requested resource does not exist or is no longer available.",
                request.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDataIntegrityViolation(DataIntegrityViolationException exception,
                                               HttpServletRequest request,
                                               Model model) {
        log.error("Data integrity violation on [{} {}]", request.getMethod(), request.getRequestURI(), exception);
        populateErrorModel(model, "Operation could not be completed",
                "This action failed because the data is still linked to other records or violates a database constraint.",
                request.getRequestURI());
        return "error/500";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied() {
        return "access-denied";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception exception, HttpServletRequest request, Model model) {
        log.error("Unexpected error on [{} {}]", request.getMethod(), request.getRequestURI(), exception);
        populateErrorModel(model, "Unexpected server error",
                "Something went wrong while processing your request. Please try again.",
                request.getRequestURI());
        return "error/500";
    }

    private void populateErrorModel(Model model, String title, String message, String path) {
        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
        model.addAttribute("requestPath", path);
    }
}
