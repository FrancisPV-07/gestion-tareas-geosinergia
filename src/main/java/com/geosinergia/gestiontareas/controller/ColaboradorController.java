package com.geosinergia.gestiontareas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.geosinergia.gestiontareas.dto.ColaboradorEditForm;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.dto.ColaboradorForm;
import com.geosinergia.gestiontareas.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/colaboradores")

public class ColaboradorController {

    private final UsuarioService usuarioService;

    public ColaboradorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarColaboradores(Model model) {

        model.addAttribute(
                "colaboradores",
                usuarioService.listarColaboradores());

        return "colaboradores/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {

        if (!model.containsAttribute("colaboradorForm")) {
            model.addAttribute(
                    "colaboradorForm",
                    new ColaboradorForm());
        }

        return "colaboradores/registro";
    }

    @PostMapping("/nuevo")
    public String registrarColaborador(
            @Valid @ModelAttribute("colaboradorForm") ColaboradorForm formulario,
            BindingResult resultado,
            RedirectAttributes redirectAttributes) {

        if (!formulario.getPassword()
                .equals(formulario.getConfirmarPassword())) {

            resultado.rejectValue(
                    "confirmarPassword",
                    "password.no.coincide",
                    "Las contraseñas no coinciden.");
        }

        if (resultado.hasErrors()) {
            return "colaboradores/registro";
        }

        try {

            usuarioService.registrarColaborador(
                    formulario.getNombres(),
                    formulario.getApellidos(),
                    formulario.getCorreo(),
                    formulario.getPassword(),
                    formulario.getCargo());

        } catch (IllegalArgumentException e) {

            resultado.rejectValue(
                    "correo",
                    "correo.duplicado",
                    e.getMessage());

            return "colaboradores/registro";
        }

        redirectAttributes.addFlashAttribute(
                "exito",
                "Colaborador registrado correctamente.");

        return "redirect:/colaboradores";
    }

    @GetMapping("/{id}/editar")
    public String mostrarEdicion(
            @PathVariable Long id,
            Model model) {

        Usuario usuario = usuarioService.buscarColaborador(id);

        ColaboradorEditForm formulario = new ColaboradorEditForm();

        formulario.setNombres(usuario.getNombres());
        formulario.setApellidos(usuario.getApellidos());
        formulario.setCorreo(usuario.getCorreo());
        formulario.setCargo(usuario.getCargo());
        formulario.setActivo(usuario.getActivo());

        model.addAttribute(
                "colaboradorEditForm",
                formulario);

        model.addAttribute(
                "idUsuario",
                id);

        return "colaboradores/editar";
    }

    @PostMapping("/{id}/editar")
    public String actualizarColaborador(
            @PathVariable Long id,
            @Valid @ModelAttribute("colaboradorEditForm") ColaboradorEditForm formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {

            model.addAttribute("idUsuario", id);

            return "colaboradores/editar";
        }

        try {

            usuarioService.actualizarColaborador(
                    id,
                    formulario);

        } catch (IllegalArgumentException e) {

            resultado.rejectValue(
                    "correo",
                    "correo.duplicado",
                    e.getMessage());

            model.addAttribute("idUsuario", id);

            return "colaboradores/editar";
        }

        redirectAttributes.addFlashAttribute(
                "exito",
                "Colaborador actualizado correctamente.");

        return "redirect:/colaboradores";
    }
}