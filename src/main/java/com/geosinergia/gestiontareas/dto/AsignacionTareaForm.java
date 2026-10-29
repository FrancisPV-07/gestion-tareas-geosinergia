
package com.geosinergia.gestiontareas.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public class AsignacionTareaForm {

    // Tarea que se desea asignar
    @NotNull(message = "Debe seleccionar una tarea.")
    private Long idTarea;

    // Colaborador responsable
    @NotNull(message = "Debe seleccionar un colaborador.")
    private Long idResponsable;

    // Prioridad de la tarea
    @NotNull(message = "Debe seleccionar una prioridad.")
    private Long idPrioridad;

    // Fecha limite de la tarea
    @NotNull(message = "Debe seleccionar una fecha limite.")
    @FutureOrPresent(message = "La fecha limite no puede ser anterior a hoy.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaLimite;

    // Getters y setters

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long idResponsable) {
        this.idResponsable = idResponsable;
    }

    public Long getIdPrioridad() {
        return idPrioridad;
    }

    public void setIdPrioridad(Long idPrioridad) {
        this.idPrioridad = idPrioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }
}
