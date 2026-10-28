
package com.geosinergia.gestiontareas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.geosinergia.gestiontareas.dto.TareaForm;
import com.geosinergia.gestiontareas.service.TareaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {

        if (!model.containsAttribute("tareaForm")) {
            model.addAttribute("tareaForm", new TareaForm());
        }

        return "tareas/registro";
    }

    @PostMapping("/nueva")
    public String registrarTarea(
            @Valid @ModelAttribute("tareaForm") TareaForm formulario,
            BindingResult resultado,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "tareas/registro";
        }

        try {

            tareaService.registrarTarea(
                    formulario.getTitulo(),
                    formulario.getDescripcion(),
                    authentication.getName());

        } catch (IllegalArgumentException e) {

            resultado.reject(
                    "tarea.registro.error",
                    e.getMessage());

            return "tareas/registro";

        } catch (IllegalStateException e) {

            resultado.reject(
                    "tarea.estado.error",
                    "No se pudo registrar la tarea. "
                            + "Verifica la configuracion de los estados.");

            return "tareas/registro";
        }

        redirectAttributes.addFlashAttribute(
                "exito",
                "Tarea registrada correctamente.");

        return "redirect:/tareas/nueva";
    }
}
