package com.geosinergia.gestiontareas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class ColaboradorEditForm {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80, message = "Los nombres no pueden superar los 80 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?:[ '-][\\p{L}]+)*$", message = "Los nombres solo pueden contener letras")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}]+(?:[ '-][\\p{L}]+)*$", message = "Los apellidos solo pueden contener letras")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@geosinergia\\.com\\.pe$", message = "El correo debe pertenecer al dominio @geosinergia.com.pe")
    private String correo;

    @Size(max = 100, message = "El cargo no puede superar los 100 caracteres")
    @Pattern(regexp = "^$|^[\\p{L}]+(?:[ -][\\p{L}]+)*$", message = "El cargo solo puede contener letras")
    private String cargo;

    @NotNull
    private Boolean activo;

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}