package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.geosinergia.gestiontareas.entity.HistorialTarea;
import com.geosinergia.gestiontareas.entity.Prioridad;
import com.geosinergia.gestiontareas.entity.Tarea;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.repository.HistorialTareaRepository;
import com.geosinergia.gestiontareas.repository.PrioridadRepository;
import com.geosinergia.gestiontareas.repository.TareaRepository;
import com.geosinergia.gestiontareas.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class TareaAsignacionHistorialIntegracionTest {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrioridadRepository prioridadRepository;

    @Autowired
    private HistorialTareaRepository historialRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    void asignarDesasignarYVolverAAsignarRegistraHistorialEnMySQL() {

        // 1. Obtener un coordinador activo
        Usuario coordinador = usuarioRepository
                .findByRol_NombreOrderByApellidosAsc("COORDINADOR")
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe coordinador activo para la prueba."));

        // 2. Obtener un colaborador activo
        Usuario colaborador = usuarioRepository
                .findByRol_NombreOrderByApellidosAsc("COLABORADOR")
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe colaborador activo para la prueba."));

        // 3. Obtener una prioridad existente
        Prioridad prioridad = prioridadRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe prioridad para la prueba."));

        // 4. Crear tarea temporal
        Tarea tarea = tareaService.registrarTarea(
                "PRUEBA AUTOMATIZADA - ASIGNACION",
                "Validar historial de asignaciones con rollback.",
                coordinador.getCorreo());

        Long idTarea = tarea.getIdTarea();

        int cantidadInicial = historialRepository
                .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                        idTarea)
                .size();

        LocalDate primeraFecha = LocalDate.now().plusDays(14);

        // ==========================================
        // 5. PRIMERA ASIGNACION
        // ==========================================

        tareaService.asignarTarea(
                idTarea,
                colaborador.getIdUsuario(),
                prioridad.getIdPrioridad(),
                primeraFecha,
                coordinador.getCorreo());

        entityManager.flush();
        entityManager.clear();

        List<HistorialTarea> historialPrimero =
                historialRepository
                        .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                                idTarea);

        assertEquals(
                cantidadInicial + 1,
                historialPrimero.size());

        HistorialTarea primerEvento = historialPrimero.get(0);

        assertEquals("ASIGNACION", primerEvento.getTipoEvento());

        assertEquals(
                coordinador.getIdUsuario(),
                primerEvento.getUsuarioActor().getIdUsuario());

        Map<String, Object> anteriorPrimero =
                primerEvento.getValorAnterior();

        assertNull(anteriorPrimero.get("responsableId"));
        assertNull(anteriorPrimero.get("prioridadId"));
        assertNull(anteriorPrimero.get("fechaLimite"));

        Map<String, Object> nuevoPrimero =
                primerEvento.getValorNuevo();

        assertEquals(
                colaborador.getIdUsuario().longValue(),
                ((Number) nuevoPrimero.get("responsableId"))
                        .longValue());

        assertEquals(
                prioridad.getIdPrioridad().longValue(),
                ((Number) nuevoPrimero.get("prioridadId"))
                        .longValue());

        assertEquals(
                primeraFecha.toString(),
                nuevoPrimero.get("fechaLimite"));

        assertEquals("Pendiente", nuevoPrimero.get("estado"));

        // ==========================================
        // 6. DESASIGNACION
        // ==========================================

        tareaService.desasignarTarea(
                idTarea,
                "Prueba de desasignacion con rollback",
                coordinador.getCorreo());

        entityManager.flush();
        entityManager.clear();

        List<HistorialTarea> historialSegundo =
                historialRepository
                        .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                                idTarea);

        assertEquals(
                cantidadInicial + 2,
                historialSegundo.size());

        assertEquals(
                "DESASIGNACION",
                historialSegundo.get(0).getTipoEvento());

        Tarea tareaSinAsignar = tareaRepository
                .findByIdTarea(idTarea)
                .orElseThrow();

        assertNull(tareaSinAsignar.getResponsable());
        assertNull(tareaSinAsignar.getPrioridad());
        assertNull(tareaSinAsignar.getFechaLimite());

        // ==========================================
        // 7. NUEVA ASIGNACION
        // ==========================================

        LocalDate segundaFecha = LocalDate.now().plusDays(21);

        tareaService.asignarTarea(
                idTarea,
                colaborador.getIdUsuario(),
                prioridad.getIdPrioridad(),
                segundaFecha,
                coordinador.getCorreo());

        entityManager.flush();
        entityManager.clear();

        List<HistorialTarea> historialFinal =
                historialRepository
                        .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                                idTarea);

        // Deben haberse agregado exactamente 3 eventos
        assertEquals(
                cantidadInicial + 3,
                historialFinal.size());

        // Orden: del mas reciente al mas antiguo
        assertEquals(
                "ASIGNACION",
                historialFinal.get(0).getTipoEvento());

        assertEquals(
                "DESASIGNACION",
                historialFinal.get(1).getTipoEvento());

        assertEquals(
                "ASIGNACION",
                historialFinal.get(2).getTipoEvento());

        HistorialTarea nuevaAsignacion = historialFinal.get(0);

        assertEquals(
                coordinador.getIdUsuario(),
                nuevaAsignacion.getUsuarioActor().getIdUsuario());

        Map<String, Object> anteriorSegundo =
                nuevaAsignacion.getValorAnterior();

        assertNull(anteriorSegundo.get("responsableId"));
        assertNull(anteriorSegundo.get("prioridadId"));
        assertNull(anteriorSegundo.get("fechaLimite"));

        Map<String, Object> nuevoSegundo =
                nuevaAsignacion.getValorNuevo();

        assertEquals(
                colaborador.getIdUsuario().longValue(),
                ((Number) nuevoSegundo.get("responsableId"))
                        .longValue());

        assertEquals(
                prioridad.getIdPrioridad().longValue(),
                ((Number) nuevoSegundo.get("prioridadId"))
                        .longValue());

        assertEquals(
                segundaFecha.toString(),
                nuevoSegundo.get("fechaLimite"));

        // 8. Validar estado final de la tarea
        Tarea tareaFinal = tareaRepository
                .findByIdTarea(idTarea)
                .orElseThrow();

        assertEquals(
                colaborador.getIdUsuario(),
                tareaFinal.getResponsable().getIdUsuario());

        assertEquals(
                segundaFecha,
                tareaFinal.getFechaLimite());

        assertEquals(
                "Pendiente",
                tareaFinal.getEstado().getNombre());
    }
}
