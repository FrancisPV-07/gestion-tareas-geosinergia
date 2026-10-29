
package com.geosinergia.gestiontareas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geosinergia.gestiontareas.entity.EstadoTarea;

public interface EstadoTareaRepository
        extends JpaRepository<EstadoTarea, Long> {

    Optional<EstadoTarea> findByNombre(String nombre);
}
