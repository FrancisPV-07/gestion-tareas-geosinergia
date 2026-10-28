
package com.geosinergia.gestiontareas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TareaForm {

    @NotBlank(message = "El titulo de la tarea es obligatorio.")
    @Size(max = 150, message = "El titulo no puede superar los 150 caracteres.")
    private String titulo;

    @NotBlank(message = "La descripcion de la tarea es obligatoria.")
    private String descripcion;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
