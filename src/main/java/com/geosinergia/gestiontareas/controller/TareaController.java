
package com.geosinergia.gestiontareas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.geosinergia.gestiontareas.dto.AsignacionTareaForm;
import com.geosinergia.gestiontareas.dto.TareaForm;
import com.geosinergia.gestiontareas.service.TareaService;
import com.geosinergia.gestiontareas.entity.Tarea;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/tareas")
public class TareaController {

        private final TareaService tareaService;

        public TareaController(TareaService tareaService) {
                this.tareaService = tareaService;
        }

        // ==========================================
        // HU-04: MOSTRAR REGISTRO DE TAREA
        // ==========================================

        @GetMapping("/nueva")
        public String mostrarFormulario(Model model) {

                if (!model.containsAttribute("tareaForm")) {
                        model.addAttribute("tareaForm", new TareaForm());
                }

                return "tareas/registro";
        }

        // ==========================================
        // HU-04: REGISTRAR TAREA
        // ==========================================

        @PostMapping("/nueva")
        public String registrarTarea(
                        @Valid @ModelAttribute("tareaForm") TareaForm formulario,
                        BindingResult resultado,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {

                if (resultado.hasErrors()) {
                        return "tareas/registro";
                }

                Tarea tareaRegistrada;

                try {

                        tareaRegistrada = tareaService.registrarTarea(
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

                // Mensaje de confirmacion
                redirectAttributes.addFlashAttribute(
                                "exito",
                                "Tarea registrada correctamente.");

                // Datos para ofrecer la asignacion inmediata
                redirectAttributes.addFlashAttribute(
                                "idTareaRegistrada",
                                tareaRegistrada.getIdTarea());

                redirectAttributes.addFlashAttribute(
                                "tituloTareaRegistrada",
                                tareaRegistrada.getTitulo());

                return "redirect:/tareas/nueva";
        }

        // ==========================================
        // HU-05: MOSTRAR FORMULARIO DE ASIGNACION
        // ==========================================

        @GetMapping("/asignar")
        public String mostrarFormularioAsignacion(
                        @RequestParam(name = "idTarea", required = false) Long idTarea,
                        Model model) {

                // Preparar el formulario
                AsignacionTareaForm formulario = new AsignacionTareaForm();

                model.addAttribute(
                                "asignacionTareaForm",
                                formulario);

                // Cargar tareas, colaboradores y prioridades
                List<Tarea> tareasDisponibles = cargarOpcionesAsignacion(model);

                // Verificar si se recibio una tarea preseleccionada
                if (idTarea != null) {

                        Tarea tareaSeleccionada = tareasDisponibles
                                        .stream()
                                        .filter(tarea -> idTarea.equals(tarea.getIdTarea()))
                                        .findFirst()
                                        .orElse(null);

                        if (tareaSeleccionada != null) {

                                // Preseleccionar la tarea en el formulario
                                formulario.setIdTarea(
                                                tareaSeleccionada.getIdTarea());

                                // Enviar sus datos a la vista
                                model.addAttribute(
                                                "tareaPreseleccionada",
                                                tareaSeleccionada);

                        } else {

                                model.addAttribute(
                                                "avisoAsignacion",
                                                "La tarea indicada no esta disponible "
                                                                + "para asignacion.");
                        }
                }

                return "tareas/asignacion";
        }

        // ==========================================
        // HU-05: PROCESAR ASIGNACION
        // ==========================================

        @PostMapping("/asignar")
        public String asignarTarea(
                        @Valid @ModelAttribute("asignacionTareaForm") AsignacionTareaForm formulario,
                        BindingResult resultado,
                        @RequestParam(name = "tareaFijada", defaultValue = "false") boolean tareaFijada,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                // Verificar las validaciones del formulario
                if (resultado.hasErrors()) {

                        return mostrarAsignacionConErrores(
                                        model, formulario, tareaFijada);
                }

                try {

                        tareaService.asignarTarea(
                                        formulario.getIdTarea(),
                                        formulario.getIdResponsable(),
                                        formulario.getIdPrioridad(),
                                        formulario.getFechaLimite());

                } catch (IllegalArgumentException e) {

                        resultado.reject(
                                        "tarea.asignacion.error",
                                        e.getMessage());

                        return mostrarAsignacionConErrores(
                                        model, formulario, tareaFijada);
                }

                redirectAttributes.addFlashAttribute(
                                "exito",
                                "Tarea asignada correctamente.");

                return "redirect:/tareas/asignar";
        }

        // ==========================================
        // HU-05: CONSERVAR DATOS ANTE ERRORES
        // ==========================================

        private String mostrarAsignacionConErrores(
                        Model model,
                        AsignacionTareaForm formulario,
                        boolean tareaFijada) {

                // Recuperar opciones del formulario
                List<Tarea> tareasDisponibles = cargarOpcionesAsignacion(model);

                // Restaurar la tarea preseleccionada
                if (tareaFijada && formulario.getIdTarea() != null) {

                        tareasDisponibles.stream()
                                        .filter(tarea -> formulario.getIdTarea()
                                                        .equals(tarea.getIdTarea()))
                                        .findFirst()
                                        .ifPresent(tarea -> model.addAttribute(
                                                        "tareaPreseleccionada",
                                                        tarea));
                }

                return "tareas/asignacion";
        }

        // ==========================================
        // HU-05: CARGAR OPCIONES DEL FORMULARIO
        // ==========================================

        private List<Tarea> cargarOpcionesAsignacion(Model model) {

                // Consultar tareas disponibles
                List<Tarea> tareasDisponibles = tareaService.listarTareasPendientesAsignacion();

                model.addAttribute(
                                "tareasDisponibles",
                                tareasDisponibles);

                // Consultar colaboradores activos
                model.addAttribute(
                                "colaboradores",
                                tareaService.listarColaboradoresActivos());

                // Consultar prioridades
                model.addAttribute(
                                "prioridades",
                                tareaService.listarPrioridades());

                return tareasDisponibles;
        }

        // ==========================================
        // HU-07: CONSULTAR MIS TAREAS
        // ==========================================

        @GetMapping("/mis-tareas")
        public String mostrarMisTareas(
                        Model model,
                        Authentication authentication) {

                // Obtener las tareas del usuario autenticado
                List<Tarea> tareasAsignadas = tareaService.consultarTareasAsignadas(
                                authentication.getName());

                // Enviar las tareas a la vista
                model.addAttribute(
                                "tareasAsignadas",
                                tareasAsignadas);

                // Mostrar la pantalla Mis tareas
                return "tareas/mis-tareas";
        }
}
