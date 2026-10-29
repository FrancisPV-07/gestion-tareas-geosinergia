
package com.geosinergia.gestiontareas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.geosinergia.gestiontareas.entity.Tarea;

public interface TareaRepository
                extends JpaRepository<Tarea, Long> {

        // Consultar tareas pendientes sin responsable,
        // incluyendo el estado y el usuario creador.
        @EntityGraph(attributePaths = { "estado", "creador" })
        List<Tarea> findByResponsableIsNullAndEstado_NombreOrderByFechaCreacionDesc(
                        String nombreEstado);

}
