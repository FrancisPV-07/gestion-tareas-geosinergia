
package com.geosinergia.gestiontareas.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geosinergia.gestiontareas.entity.Tarea;

public interface TareaRepository
        extends JpaRepository<Tarea, Long> {

}
