package com.geosinergia.gestiontareas.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String mostrarLogin(
            Authentication authentication,
            HttpServletRequest request,
            Model model) {

        /*
         * Si el usuario ya inició sesión,
         * no permitimos que vuelva al login.
         */
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "redirect:/";
        }


        HttpSession session =
                request.getSession(false);


        /*
         * Mensaje de cierre de sesión exitoso.
         */
        if (session != null
                && Boolean.TRUE.equals(
                        session.getAttribute("logoutSuccess"))) {

            model.addAttribute(
                    "logoutSuccess",
                    true);

            session.removeAttribute(
                    "logoutSuccess");
        }


        /*
         * Mensaje de error de autenticación.
         *
         * Puede ser:
         * - Correo o contraseña incorrectos.
         * - Cuenta inactiva.
         */
        if (session != null) {

            Object loginError =
                    session.getAttribute("loginError");


            if (loginError instanceof String mensajeError) {

                model.addAttribute(
                        "loginError",
                        mensajeError);

                session.removeAttribute(
                        "loginError");
            }
        }


        return "login";
    }
}