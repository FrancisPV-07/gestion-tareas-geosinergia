package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
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
class TareaReasignacionIntegracionTest {

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
    void reasignarTareaActualizaResponsableEHistorialEnMySQL() {

        // 1. Obtener coordinador activo
        Usuario coordinador = usuarioRepository
                .findByRol_NombreOrderByApellidosAsc("COORDINADOR")
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe un coordinador activo para la prueba."));

        // 2. Obtener colaboradores activos
        List<Usuario> colaboradores = usuarioRepository
                .findByRol_NombreOrderByApellidosAsc("COLABORADOR")
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .toList();

        // 3. Seleccionar una tarea elegible
        Tarea tarea = tareaRepository.findAll()
                .stream()
                .filter(t -> t.getResponsable() != null)
                .filter(t ->
                        "Pendiente".equals(t.getEstado().getNombre())
                        || "En proceso".equals(
                                t.getEstado().getNombre()))
                .filter(t -> colaboradores.stream().anyMatch(
                        u -> !u.getIdUsuario().equals(
                                t.getResponsable().getIdUsuario())))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay una tarea asignada y otro colaborador "
                        + "activo para ejecutar la prueba."));

        Long idTarea = tarea.getIdTarea();
        Long idResponsableAnterior =
                tarea.getResponsable().getIdUsuario();

        String estadoAnterior = tarea.getEstado().getNombre();

        // Conservar datos originales
        Long idPrioridadAnterior = tarea.getPrioridad() == null
                ? null
                : tarea.getPrioridad().getIdPrioridad();

        LocalDate fechaLimiteAnterior = tarea.getFechaLimite();

        // 4. Seleccionar otro colaborador activo
        Usuario nuevoResponsable = colaboradores.stream()
                .filter(u -> !u.getIdUsuario().equals(
                        idResponsableAnterior))
                .findFirst()
                .orElseThrow();

        Long idNuevoResponsable = nuevoResponsable.getIdUsuario();

        // 5. Consultar cantidad inicial de eventos
        int cantidadAnterior = historialRepository
                .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                        idTarea)
                .size();

        // 6. Ejecutar reasignacion
        tareaService.reasignarTarea(
                idTarea,
                idNuevoResponsable,
                "Prueba de integracion con rollback",
                coordinador.getCorreo());

        // 7. Forzar escritura SQL y limpiar contexto JPA
        entityManager.flush();
        entityManager.clear();

        // 8. Consultar nuevamente desde MySQL
        Tarea tareaActualizada = tareaRepository
                .findByIdTarea(idTarea)
                .orElseThrow();

        // Verificar responsable y estado
        assertEquals(
                idNuevoResponsable,
                tareaActualizada.getResponsable().getIdUsuario());

        assertEquals(
                "Pendiente",
                tareaActualizada.getEstado().getNombre());

        // Verificar prioridad y fecha limite
        Long idPrioridadActual = tareaActualizada.getPrioridad() == null
                ? null
                : tareaActualizada.getPrioridad().getIdPrioridad();

        assertEquals(
                idPrioridadAnterior,
                idPrioridadActual);

        assertEquals(
                fechaLimiteAnterior,
                tareaActualizada.getFechaLimite());

        // 9. Verificar historial
        List<HistorialTarea> historialNuevo = historialRepository
                .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                        idTarea);

        assertEquals(
                cantidadAnterior + 1,
                historialNuevo.size());

        HistorialTarea evento = historialNuevo.get(0);

        assertEquals(
                "REASIGNACION",
                evento.getTipoEvento());

        assertEquals(
                coordinador.getIdUsuario(),
                evento.getUsuarioActor().getIdUsuario());

        assertEquals(
                "Prueba de integracion con rollback",
                evento.getMotivo());

        // Verificar valores JSON anteriores
        assertEquals(
                idResponsableAnterior.longValue(),
                ((Number) evento.getValorAnterior()
                        .get("responsableId")).longValue());

        assertEquals(
                estadoAnterior,
                evento.getValorAnterior().get("estado"));

        // Verificar valores JSON nuevos
        assertEquals(
                idNuevoResponsable.longValue(),
                ((Number) evento.getValorNuevo()
                        .get("responsableId")).longValue());

        assertEquals(
                "Pendiente",
                evento.getValorNuevo().get("estado"));
    }
}
