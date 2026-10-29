
package com.geosinergia.gestiontareas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
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

        // ==========================================
        // HU-07: CONSULTAR TAREAS ASIGNADAS
        // ==========================================

        @Transactional(readOnly = true)
        public List<Tarea> consultarTareasAsignadas(
                        String correoColaborador) {

                // Verificar el correo del usuario autenticado
                if (correoColaborador == null
                                || correoColaborador.isBlank()) {

                        throw new IllegalArgumentException(
                                        "No se pudo identificar al usuario autenticado.");
                }

                // Buscar al usuario en la base de datos
                Usuario colaborador = usuarioRepository
                                .findByCorreo(correoColaborador)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El usuario autenticado no existe."));

                // Verificar que sea un colaborador activo
                if (!Boolean.TRUE.equals(colaborador.getActivo())
                                || !"COLABORADOR".equals(
                                                colaborador.getRol().getNombre())) {

                        throw new IllegalArgumentException(
                                        "No tiene permisos para consultar tareas asignadas.");
                }

                // Consultar únicamente sus tareas
                return tareaRepository
                                .findByResponsable_CorreoOrderByFechaCreacionDesc(
                                                colaborador.getCorreo());
        }



        // ==========================================
        // HU-08: CONSULTAR DETALLE DE TAREA
        // ==========================================

        @Transactional(readOnly = true)
        public Tarea obtenerDetalleTarea(
                Long idTarea,
                String correoUsuario) {

                // Validar identificador y usuario autenticado
                if (idTarea == null || idTarea <= 0) {
                throw new IllegalArgumentException(
                        "El identificador de la tarea no es valido.");
                }

                if (correoUsuario == null || correoUsuario.isBlank()) {
                throw new AccessDeniedException(
                        "No se pudo identificar al usuario autenticado.");
                }

                // Obtener al usuario autenticado
                Usuario usuario = usuarioRepository
                        .findByCorreo(correoUsuario)
                        .orElseThrow(() -> new AccessDeniedException(
                                "El usuario autenticado no existe."));

                if (!Boolean.TRUE.equals(usuario.getActivo())) {
                throw new AccessDeniedException(
                        "El usuario no tiene acceso a esta funcion.");
                }

                // Buscar la tarea
                Tarea tarea = tareaRepository
                        .findByIdTarea(idTarea)
                        .orElseThrow(() -> new NoSuchElementException(
                                "La tarea solicitada no existe."));

                String rol = usuario.getRol().getNombre();

                // El coordinador puede consultar cualquier tarea
                if ("COORDINADOR".equals(rol)) {
                return tarea;
                }

                // El colaborador solo puede consultar sus tareas
                if ("COLABORADOR".equals(rol)
                        && tarea.getResponsable() != null
                        && usuario.getIdUsuario().equals(
                                tarea.getResponsable().getIdUsuario())) {

                return tarea;
                }

                // Denegar cualquier otro acceso
                throw new AccessDeniedException(
                        "No tiene permisos para consultar esta tarea.");
        }

}
