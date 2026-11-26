package com.geosinergia.gestiontareas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.geosinergia.gestiontareas.entity.HistorialTarea;

public interface HistorialTareaRepository
        extends JpaRepository<HistorialTarea, Long> {

    @EntityGraph(attributePaths = { "usuarioActor" })
    List<HistorialTarea>
        findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
            Long idTarea);
}
