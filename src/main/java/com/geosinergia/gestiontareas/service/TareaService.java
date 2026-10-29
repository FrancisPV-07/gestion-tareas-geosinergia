
package com.geosinergia.gestiontareas.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geosinergia.gestiontareas.entity.EstadoTarea;
import com.geosinergia.gestiontareas.entity.Prioridad;
import com.geosinergia.gestiontareas.entity.Tarea;
import com.geosinergia.gestiontareas.entity.Usuario;

import com.geosinergia.gestiontareas.repository.EstadoTareaRepository;
import com.geosinergia.gestiontareas.repository.PrioridadRepository;
import com.geosinergia.gestiontareas.repository.TareaRepository;
import com.geosinergia.gestiontareas.repository.UsuarioRepository;

@Service
public class TareaService {

        private final TareaRepository tareaRepository;
        private final EstadoTareaRepository estadoTareaRepository;
        private final UsuarioRepository usuarioRepository;
        private final PrioridadRepository prioridadRepository;

        public TareaService(
                        TareaRepository tareaRepository,
                        EstadoTareaRepository estadoTareaRepository,
                        UsuarioRepository usuarioRepository,
                        PrioridadRepository prioridadRepository) {

                this.tareaRepository = tareaRepository;
                this.estadoTareaRepository = estadoTareaRepository;
                this.usuarioRepository = usuarioRepository;
                this.prioridadRepository = prioridadRepository;
        }

        // ==========================================
        // HU-04: REGISTRAR TAREA
        // ==========================================

        @Transactional
        public Tarea registrarTarea(
                        String titulo,
                        String descripcion,
                        String correoCoordinador) {

                // Validar los datos de la tarea
                if (titulo == null || titulo.isBlank()) {
                        throw new IllegalArgumentException(
                                        "El titulo de la tarea es obligatorio.");
                }

                if (titulo.trim().length() > 150) {
                        throw new IllegalArgumentException(
                                        "El titulo no puede superar los 150 caracteres.");
                }

                if (descripcion == null || descripcion.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La descripcion de la tarea es obligatoria.");
                }

                // Identificar al usuario autenticado
                Usuario creador = usuarioRepository
                                .findByCorreo(correoCoordinador)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El usuario autenticado no existe."));

                // Verificar que sea un coordinador activo
                if (!Boolean.TRUE.equals(creador.getActivo())
                                || !"COORDINADOR".equals(
                                                creador.getRol().getNombre())) {

                        throw new IllegalArgumentException(
                                        "No tiene permisos para registrar tareas.");
                }

                // Consultar el estado inicial
                EstadoTarea estadoPendiente = estadoTareaRepository
                                .findByNombre("Pendiente")
                                .orElseThrow(() -> new IllegalStateException(
                                                "No se encontro el estado Pendiente."));

                // Construir la nueva tarea
                Tarea tarea = new Tarea();

                tarea.setTitulo(titulo.trim());
                tarea.setDescripcion(descripcion.trim());
                tarea.setCreador(creador);
                tarea.setEstado(estadoPendiente);

                // La asignacion se realizara en HU-05
                tarea.setResponsable(null);
                tarea.setPrioridad(null);
                tarea.setFechaLimite(null);

                // Guardar en MySQL
                return tareaRepository.save(tarea);
        }

        // ==========================================
        // HU-05: CONSULTAR TAREAS SIN ASIGNAR
        // ==========================================

        @Transactional(readOnly = true)
        public List<Tarea> listarTareasPendientesAsignacion() {

                return tareaRepository
                                .findByResponsableIsNullAndEstado_NombreOrderByFechaCreacionDesc(
                                                "Pendiente");
        }

        // ==========================================
        // HU-05: CONSULTAR COLABORADORES ACTIVOS
        // ==========================================

        @Transactional(readOnly = true)
        public List<Usuario> listarColaboradoresActivos() {

                return usuarioRepository
                                .findByRol_NombreOrderByApellidosAsc("COLABORADOR")
                                .stream()
                                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                                .toList();
        }

        // ==========================================
        // HU-05: CONSULTAR PRIORIDADES
        // ==========================================

        @Transactional(readOnly = true)
        public List<Prioridad> listarPrioridades() {

                return prioridadRepository.findAll();
        }

        // ==========================================
        // HU-05: ASIGNAR TAREA
        // ==========================================

        @Transactional
        public Tarea asignarTarea(
                        Long idTarea,
                        Long idResponsable,
                        Long idPrioridad,
                        LocalDate fechaLimite) {

                // Validar los datos de asignacion
                if (idTarea == null
                                || idResponsable == null
                                || idPrioridad == null
                                || fechaLimite == null) {

                        throw new IllegalArgumentException(
                                        "Todos los datos de asignacion son obligatorios.");
                }

                if (fechaLimite.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException(
                                        "La fecha limite no puede ser anterior a hoy.");
                }

                // Buscar la tarea existente
                Tarea tarea = tareaRepository
                                .findById(idTarea)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "La tarea seleccionada no existe."));

                // Verificar que este pendiente y sin asignar
                if (!"Pendiente".equals(tarea.getEstado().getNombre())
                                || tarea.getResponsable() != null) {

                        throw new IllegalArgumentException(
                                        "La tarea no esta disponible para asignacion.");
                }

                // Buscar al colaborador responsable
                Usuario responsable = usuarioRepository
                                .findById(idResponsable)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El colaborador seleccionado no existe."));

                // Verificar el rol y estado del colaborador
                if (!Boolean.TRUE.equals(responsable.getActivo())
                                || !"COLABORADOR".equals(
                                                responsable.getRol().getNombre())) {

                        throw new IllegalArgumentException(
                                        "Debe seleccionar un colaborador activo.");
                }

                // Buscar la prioridad seleccionada
                Prioridad prioridad = prioridadRepository
                                .findById(idPrioridad)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "La prioridad seleccionada no existe."));

                // Actualizar los datos de asignacion
                tarea.setResponsable(responsable);
                tarea.setPrioridad(prioridad);
                tarea.setFechaLimite(fechaLimite);

                // Conservar el estado Pendiente
                return tareaRepository.save(tarea);
        }
}
