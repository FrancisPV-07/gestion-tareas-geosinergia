
package com.geosinergia.gestiontareas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geosinergia.gestiontareas.entity.EstadoTarea;
import com.geosinergia.gestiontareas.entity.Tarea;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.repository.EstadoTareaRepository;
import com.geosinergia.gestiontareas.repository.TareaRepository;
import com.geosinergia.gestiontareas.repository.UsuarioRepository;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final EstadoTareaRepository estadoTareaRepository;
    private final UsuarioRepository usuarioRepository;

    public TareaService(
            TareaRepository tareaRepository,
            EstadoTareaRepository estadoTareaRepository,
            UsuarioRepository usuarioRepository) {

        this.tareaRepository = tareaRepository;
        this.estadoTareaRepository = estadoTareaRepository;
        this.usuarioRepository = usuarioRepository;
    }

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
}
