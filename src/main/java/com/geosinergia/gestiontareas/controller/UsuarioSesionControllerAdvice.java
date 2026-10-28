package com.geosinergia.gestiontareas.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.geosinergia.gestiontareas.dto.UsuarioSesionDto;
import com.geosinergia.gestiontareas.service.UsuarioService;

@ControllerAdvice
public class UsuarioSesionControllerAdvice {

        private final UsuarioService usuarioService;

        public UsuarioSesionControllerAdvice(
                        UsuarioService usuarioService) {

                this.usuarioService = usuarioService;
        }

        @ModelAttribute
        public void agregarUsuarioActual(
                        Model model,
                        Authentication authentication) {

                if (authentication == null
                                || !authentication.isAuthenticated()
                                || authentication instanceof AnonymousAuthenticationToken) {

                        return;
                }

                UsuarioSesionDto usuario = usuarioService.obtenerUsuarioSesion(
                                authentication.getName());

                model.addAttribute(
                                "nombreUsuario",
                                usuario.nombreCompleto());

                model.addAttribute(
                                "correoUsuario",
                                usuario.correo());

                model.addAttribute(
                                "rolUsuario",
                                usuario.rol());

                model.addAttribute(
                                "inicialesUsuario",
                                usuario.iniciales());
        }
}