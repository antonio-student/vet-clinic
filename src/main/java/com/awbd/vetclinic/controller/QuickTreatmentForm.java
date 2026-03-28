package com.awbd.vetclinic.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class QuickTreatmentForm {

    @NotBlank(message = "Treatment description is mandatory")
    private String description;

    @NotNull(message = "Treatment date is mandatory")
    private LocalDate treatmentDate;

    @Min(value = 0, message = "Cost cannot be negative")
    private double cost;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(LocalDate treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
