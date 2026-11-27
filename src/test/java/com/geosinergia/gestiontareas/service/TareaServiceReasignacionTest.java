package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
class TareaServiceReasignacionTest {

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
    void reasignarTareaEnProcesoConservaDatosYRegistraEvento() {

        // Coordinador autenticado
        Rol rolCoordinador = new Rol();
        rolCoordinador.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rolCoordinador);

        // Colaboradores
        Rol rolColaborador = new Rol();
        rolColaborador.setNombre("COLABORADOR");

        Usuario responsableAnterior = new Usuario();
        responsableAnterior.setIdUsuario(2L);
        responsableAnterior.setActivo(true);
        responsableAnterior.setRol(rolColaborador);

        Usuario nuevoResponsable = new Usuario();
        nuevoResponsable.setIdUsuario(3L);
        nuevoResponsable.setActivo(true);
        nuevoResponsable.setRol(rolColaborador);

        // Estados
        EstadoTarea enProceso = new EstadoTarea();
        enProceso.setNombre("En proceso");

        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        // Datos originales
        Prioridad prioridad = new Prioridad();
        LocalDate fechaLimite = LocalDate.of(2026, 12, 10);

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setEstado(enProceso);
        tarea.setResponsable(responsableAnterior);
        tarea.setPrioridad(prioridad);
        tarea.setFechaLimite(fechaLimite);

        // Respuestas simuladas
        when(usuarioRepository.findByCorreo("coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(nuevoResponsable));

        when(estadoTareaRepository.findByNombre("Pendiente"))
                .thenReturn(Optional.of(pendiente));

        // Ejecutar
        Tarea resultado = tareaService.reasignarTarea(
                10L,
                3L,
                "Cambio de responsable",
                "coord@prueba.test");

        // Verificar cambios
        assertSame(nuevoResponsable, resultado.getResponsable());
        assertEquals("Pendiente", resultado.getEstado().getNombre());

        // Verificar que conserva los datos
        assertSame(prioridad, resultado.getPrioridad());
        assertEquals(fechaLimite, resultado.getFechaLimite());

        // Verificar persistencia solicitada
        verify(tareaRepository, times(1)).save(tarea);

        // Verificar evento único de auditoría
        verify(historialTareaService, times(1)).registrarEvento(
                eq(tarea),
                eq(coordinador),
                eq("REASIGNACION"),
                eq(Map.of(
                        "responsableId", 2L,
                        "estado", "En proceso")),
                eq(Map.of(
                        "responsableId", 3L,
                        "estado", "Pendiente")),
                eq("Cambio de responsable"));
    }

    // ==========================================
    // HU-06: PRUEBAS NEGATIVAS DE REASIGNACION
    // ==========================================

    @Test
    void colaboradorNoPuedeReasignarTarea() {

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
                () -> tareaService.reasignarTarea(
                        10L,
                        3L,
                        "Cambio de responsable",
                        "colaborador@prueba.test")
        );

        // No debe consultar ni modificar tareas
        verifyNoInteractions(
                tareaRepository,
                historialTareaService);
    }

    @Test
    void noPermiteReasignarSinMotivo() {

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
                () -> tareaService.reasignarTarea(
                        10L,
                        3L,
                        "   ",
                        "coord@prueba.test")
        );

        // El motivo debe validarse antes de modificar
        verifyNoInteractions(
                tareaRepository,
                historialTareaService);
    }

    @Test
    void noPermiteReasignarAlMismoResponsable() {

        Rol rolCoordinador = new Rol();
        rolCoordinador.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rolCoordinador);

        Rol rolColaborador = new Rol();
        rolColaborador.setNombre("COLABORADOR");

        Usuario responsable = new Usuario();
        responsable.setIdUsuario(2L);
        responsable.setActivo(true);
        responsable.setRol(rolColaborador);

        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setEstado(pendiente);
        tarea.setResponsable(responsable);

        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(responsable));

        assertThrows(
                IllegalArgumentException.class,
                () -> tareaService.reasignarTarea(
                        10L,
                        2L,
                        "Reasignacion de prueba",
                        "coord@prueba.test")
        );

        // El responsable debe permanecer igual
        assertSame(responsable, tarea.getResponsable());
        assertEquals("Pendiente",
                tarea.getEstado().getNombre());

        // No se debe guardar ni crear historial
        verify(tareaRepository, never())
                .save(any(Tarea.class));

        verifyNoInteractions(historialTareaService);
    }

    @Test
    void noPermiteReasignarEnEstadosNoPermitidos() {

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
                    () -> tareaService.reasignarTarea(
                            10L,
                            3L,
                            "Cambio de responsable",
                            "coord@prueba.test")
            );

            assertSame(responsable, tarea.getResponsable());
            assertEquals(
                    nombreEstado,
                    tarea.getEstado().getNombre());
        }

        verify(tareaRepository, never())
                .save(any(Tarea.class));

        verifyNoInteractions(historialTareaService);
    }

    @Test
    void noPermiteReasignarAColaboradorInactivo() {

        Rol rolCoordinador = new Rol();
        rolCoordinador.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rolCoordinador);

        Rol rolColaborador = new Rol();
        rolColaborador.setNombre("COLABORADOR");

        Usuario responsableActual = new Usuario();
        responsableActual.setIdUsuario(2L);
        responsableActual.setActivo(true);
        responsableActual.setRol(rolColaborador);

        Usuario colaboradorInactivo = new Usuario();
        colaboradorInactivo.setIdUsuario(3L);
        colaboradorInactivo.setActivo(false);
        colaboradorInactivo.setRol(rolColaborador);

        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setEstado(pendiente);
        tarea.setResponsable(responsableActual);

        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(colaboradorInactivo));

        assertThrows(
                IllegalArgumentException.class,
                () -> tareaService.reasignarTarea(
                        10L,
                        3L,
                        "Cambio de responsable",
                        "coord@prueba.test")
        );

        assertSame(
                responsableActual,
                tarea.getResponsable());

        assertEquals(
                "Pendiente",
                tarea.getEstado().getNombre());

        verify(tareaRepository, never())
                .save(any(Tarea.class));

        verifyNoInteractions(historialTareaService);
    }

}
