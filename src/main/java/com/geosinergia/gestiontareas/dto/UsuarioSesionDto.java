package com.geosinergia.gestiontareas.dto;

public record UsuarioSesionDto(
        String nombreCompleto,
        String correo,
        String rol,
        String iniciales
) {
}