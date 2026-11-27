package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.geosinergia.gestiontareas.entity.*;
import com.geosinergia.gestiontareas.repository.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceDesasignacionTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private EstadoTareaRepository estadoTareaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrioridadRepository prioridadRepository;

    @Mock
    private HistorialTareaService historialTareaService;

    @InjectMocks
    private TareaService tareaService;

    @Test
    void desasignarTareaEnProcesoLimpiaDatosYRegistraHistorial() {

        // 1. Coordinador autenticado
        Rol rolCoordinador = new Rol();
        rolCoordinador.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rolCoordinador);

        // 2. Responsable actual
        Usuario responsable = new Usuario();
        responsable.setIdUsuario(3L);

        // 3. Estados
        EstadoTarea enProceso = new EstadoTarea();
        enProceso.setNombre("En proceso");

        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        // 4. Datos originales
        Prioridad prioridad = new Prioridad();
        prioridad.setIdPrioridad(2L);

        LocalDate fechaLimite =
                LocalDate.of(2026, 12, 10);

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setTitulo("Tarea de prueba");
        tarea.setEstado(enProceso);
        tarea.setResponsable(responsable);
        tarea.setPrioridad(prioridad);
        tarea.setFechaLimite(fechaLimite);

        // 5. Configurar repositorios simulados
        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        when(estadoTareaRepository.findByNombre(
                "Pendiente"))
                .thenReturn(Optional.of(pendiente));

        // 6. Ejecutar desasignacion
        Tarea resultado = tareaService.desasignarTarea(
                10L,
                "Cambio de organizacion del equipo",
                "coord@prueba.test");

        // 7. Verificar datos de la tarea
        assertNull(resultado.getResponsable());
        assertNull(resultado.getPrioridad());
        assertNull(resultado.getFechaLimite());

        assertEquals(
                "Pendiente",
                resultado.getEstado().getNombre());

        // El titulo no debe modificarse
        assertEquals(
                "Tarea de prueba",
                resultado.getTitulo());

        // 8. Verificar guardado
        verify(tareaRepository, times(1))
                .save(tarea);

        // 9. Preparar valores esperados del historial
        Map<String, Object> valoresAnteriores =
                new LinkedHashMap<>();

        valoresAnteriores.put("responsableId", 3L);
        valoresAnteriores.put("prioridadId", 2L);
        valoresAnteriores.put(
                "fechaLimite", "2026-12-10");
        valoresAnteriores.put("estado", "En proceso");

        Map<String, Object> valoresNuevos =
                new LinkedHashMap<>();

        valoresNuevos.put("responsableId", null);
        valoresNuevos.put("prioridadId", null);
        valoresNuevos.put("fechaLimite", null);
        valoresNuevos.put("estado", "Pendiente");

        // 10. Verificar evento unico de auditoria
        verify(historialTareaService, times(1))
                .registrarEvento(
                        eq(tarea),
                        eq(coordinador),
                        eq("DESASIGNACION"),
                        eq(valoresAnteriores),
                        eq(valoresNuevos),
                        eq("Cambio de organizacion del equipo"));
    }

    // ==========================================
    // HU-06: PRUEBAS NEGATIVAS DE DESASIGNACION
    // ==========================================

    @Test
    void colaboradorNoPuedeDesasignarTarea() {

        Rol rol = new Rol();
        rol.setNombre("COLABORADOR");

        Usuario actor = new Usuario();
        actor.setIdUsuario(2L);
        actor.setActivo(true);
        actor.setRol(rol);

        when(usuarioRepository.findByCorreo(
                "colaborador@prueba.test"))
                .thenReturn(Optional.of(actor));

        assertThrows(
                AccessDeniedException.class,
                () -> tareaService.desasignarTarea(
                        10L,
                        "Motivo de prueba",
                        "colaborador@prueba.test")
        );

        verifyNoInteractions(
                tareaRepository,
                historialTareaService);
    }

    @Test
    void noPermiteDesasignarSinMotivo() {

        Rol rol = new Rol();
        rol.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rol);

        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        assertThrows(
                IllegalArgumentException.class,
                () -> tareaService.desasignarTarea(
                        10L,
                        "   ",
                        "coord@prueba.test")
        );

        verifyNoInteractions(
                tareaRepository,
                historialTareaService);
    }

    @Test
    void noPermiteDesasignarTareaSinResponsable() {

        Rol rol = new Rol();
        rol.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rol);

        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setEstado(pendiente);
        tarea.setResponsable(null);

        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        assertThrows(
                IllegalArgumentException.class,
                () -> tareaService.desasignarTarea(
                        10L,
                        "Retirar asignacion",
                        "coord@prueba.test")
        );

        assertNull(tarea.getResponsable());
        assertEquals(
                "Pendiente",
                tarea.getEstado().getNombre());

        verify(tareaRepository, never())
                .save(any(Tarea.class));

        verifyNoInteractions(historialTareaService);
    }

    @Test
    void noPermiteDesasignarEnEstadosNoPermitidos() {

        Rol rol = new Rol();
        rol.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rol);

        Usuario responsable = new Usuario();
        responsable.setIdUsuario(2L);

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setResponsable(responsable);

        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        String[] estadosNoPermitidos = {
                "En revisión",
                "Completada",
                "Cancelada"
        };

        for (String nombreEstado : estadosNoPermitidos) {

            EstadoTarea estado = new EstadoTarea();
            estado.setNombre(nombreEstado);
            tarea.setEstado(estado);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> tareaService.desasignarTarea(
                            10L,
                            "Retirar asignacion",
                            "coord@prueba.test")
            );

            assertSame(
                    responsable,
                    tarea.getResponsable());

            assertEquals(
                    nombreEstado,
                    tarea.getEstado().getNombre());
        }

        verify(tareaRepository, never())
                .save(any(Tarea.class));

        verifyNoInteractions(historialTareaService);
    }

}
