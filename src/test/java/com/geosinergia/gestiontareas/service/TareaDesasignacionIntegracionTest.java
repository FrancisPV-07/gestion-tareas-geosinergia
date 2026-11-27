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
import com.geosinergia.gestiontareas.entity.Tarea;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.repository.HistorialTareaRepository;
import com.geosinergia.gestiontareas.repository.TareaRepository;
import com.geosinergia.gestiontareas.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class TareaDesasignacionIntegracionTest {

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
    void desasignarTareaLimpiaCamposYRegistraHistorialEnMySQL() {

        // 1. Buscar coordinador activo
        Usuario coordinador = usuarioRepository
                .findByRol_NombreOrderByApellidosAsc("COORDINADOR")
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay un coordinador activo para la prueba."));

        // 2. Buscar tarea asignada y elegible
        Tarea tarea = tareaRepository.findAll()
                .stream()
                .filter(t -> t.getResponsable() != null)
                .filter(t -> t.getPrioridad() != null)
                .filter(t -> t.getFechaLimite() != null)
                .filter(t ->
                        "Pendiente".equals(t.getEstado().getNombre())
                        || "En proceso".equals(
                                t.getEstado().getNombre()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe una tarea asignada con prioridad "
                        + "y fecha limite para esta prueba."));

        // 3. Conservar los valores anteriores
        Long idTarea = tarea.getIdTarea();
        String tituloAnterior = tarea.getTitulo();

        Long idResponsableAnterior =
                tarea.getResponsable().getIdUsuario();

        Long idPrioridadAnterior =
                tarea.getPrioridad().getIdPrioridad();

        LocalDate fechaLimiteAnterior =
                tarea.getFechaLimite();

        String estadoAnterior =
                tarea.getEstado().getNombre();

        // 4. Contar eventos anteriores
        int cantidadAnterior = historialRepository
                .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                        idTarea)
                .size();

        // 5. Ejecutar desasignacion
        tareaService.desasignarTarea(
                idTarea,
                "Prueba de integracion de desasignacion con rollback",
                coordinador.getCorreo());

        // 6. Forzar escritura SQL y limpiar contexto JPA
        entityManager.flush();
        entityManager.clear();

        // 7. Consultar tarea nuevamente desde MySQL
        Tarea tareaActualizada = tareaRepository
                .findByIdTarea(idTarea)
                .orElseThrow();

        assertNull(tareaActualizada.getResponsable());
        assertNull(tareaActualizada.getPrioridad());
        assertNull(tareaActualizada.getFechaLimite());

        assertEquals(
                "Pendiente",
                tareaActualizada.getEstado().getNombre());

        assertEquals(
                tituloAnterior,
                tareaActualizada.getTitulo());

        // 8. Verificar un nuevo evento de historial
        List<HistorialTarea> historialNuevo =
                historialRepository
                        .findByTarea_IdTareaOrderByFechaEventoDescIdHistorialDesc(
                                idTarea);

        assertEquals(
                cantidadAnterior + 1,
                historialNuevo.size());

        HistorialTarea evento = historialNuevo.get(0);

        assertEquals(
                "DESASIGNACION",
                evento.getTipoEvento());

        assertEquals(
                coordinador.getIdUsuario(),
                evento.getUsuarioActor().getIdUsuario());

        assertEquals(
                "Prueba de integracion de desasignacion con rollback",
                evento.getMotivo());

        // 9. Verificar valores anteriores del JSON
        Map<String, Object> anterior =
                evento.getValorAnterior();

        assertEquals(
                idResponsableAnterior.longValue(),
                ((Number) anterior.get("responsableId")).longValue());

        assertEquals(
                idPrioridadAnterior.longValue(),
                ((Number) anterior.get("prioridadId")).longValue());

        assertEquals(
                fechaLimiteAnterior.toString(),
                anterior.get("fechaLimite"));

        assertEquals(
                estadoAnterior,
                anterior.get("estado"));

        // 10. Verificar nuevos valores del JSON
        Map<String, Object> nuevo =
                evento.getValorNuevo();

        assertTrue(nuevo.containsKey("responsableId"));
        assertTrue(nuevo.containsKey("prioridadId"));
        assertTrue(nuevo.containsKey("fechaLimite"));

        assertNull(nuevo.get("responsableId"));
        assertNull(nuevo.get("prioridadId"));
        assertNull(nuevo.get("fechaLimite"));

        assertEquals(
                "Pendiente",
                nuevo.get("estado"));
    }
}
