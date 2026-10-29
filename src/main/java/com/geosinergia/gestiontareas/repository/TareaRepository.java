
package com.geosinergia.gestiontareas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.geosinergia.gestiontareas.entity.Tarea;

public interface TareaRepository
                extends JpaRepository<Tarea, Long> {

        // ==========================================
        // HU-05: TAREAS PENDIENTES DE ASIGNACION
        // ==========================================

        @EntityGraph(attributePaths = { "estado", "creador" })
        List<Tarea> findByResponsableIsNullAndEstado_NombreOrderByFechaCreacionDesc(
                        String nombreEstado);

        // ==========================================
        // HU-07: CONSULTAR MIS TAREAS
        // ==========================================

        @EntityGraph(attributePaths = {
                        "estado",
                        "prioridad",
                        "creador"
        })
        List<Tarea> findByResponsable_CorreoOrderByFechaCreacionDesc(
                        String correo);

}
