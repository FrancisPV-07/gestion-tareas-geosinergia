package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.geosinergia.gestiontareas.entity.HistorialTarea;
import com.geosinergia.gestiontareas.entity.Tarea;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.repository.HistorialTareaRepository;
import com.geosinergia.gestiontareas.repository.TareaRepository;
import com.geosinergia.gestiontareas.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class TareaCancelacionIntegracionTest {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistorialTareaRepository historialRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    void cancelarTareaGuardaEstadoEHistorialEnMySQL() {

        Usuario coordinador = usuarioRepository
                .findByRol_NombreOrderByApellidosAsc("COORDINADOR")
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .orElseThrow();

        Tarea tarea = tareaRepository.findAll()
                .stream()
                .filter(t -> "Pendiente".equals(
                        t.getEstado().getNombre()))
                .findFirst()
                .orElseThrow();

        Long idTarea = tarea.getIdTarea();

        List<HistorialTarea> historialAnterior =
                historialRepository
                        .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                                idTarea);

        int cantidadAnterior = historialAnterior.size();

        tareaService.cancelarTarea(
                idTarea,
                "Prueba de integracion con rollback",
                coordinador.getCorreo());

        // Forzar la escritura SQL dentro de la transaccion
        entityManager.flush();
        entityManager.clear();

        Tarea tareaActualizada = tareaRepository
                .findByIdTarea(idTarea)
                .orElseThrow();

        assertEquals(
                "Cancelada",
                tareaActualizada.getEstado().getNombre());

        List<HistorialTarea> historialNuevo =
                historialRepository
                        .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                                idTarea);

        assertEquals(
                cantidadAnterior + 1,
                historialNuevo.size());

        HistorialTarea evento = historialNuevo.get(0);

        assertEquals("CANCELACION", evento.getTipoEvento());
        assertEquals(
                coordinador.getIdUsuario(),
                evento.getUsuarioActor().getIdUsuario());

        assertEquals(
                "Pendiente",
                evento.getValorAnterior().get("estado"));

        assertEquals(
                "Cancelada",
                evento.getValorNuevo().get("estado"));
    }
}
