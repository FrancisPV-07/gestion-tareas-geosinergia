package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.geosinergia.gestiontareas.entity.*;
import com.geosinergia.gestiontareas.repository.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceCancelacionTest {

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
    void cancelarPendienteActualizaEstadoYRegistraEvento() {

        Rol rol = new Rol();
        rol.setNombre("COORDINADOR");

        Usuario actor = new Usuario();
        actor.setIdUsuario(1L);
        actor.setActivo(true);
        actor.setRol(rol);

        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        EstadoTarea cancelada = new EstadoTarea();
        cancelada.setNombre("Cancelada");

        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setEstado(pendiente);

        when(usuarioRepository.findByCorreo("coord@prueba.test"))
                .thenReturn(Optional.of(actor));

        when(tareaRepository.findByIdTarea(10L))
                .thenReturn(Optional.of(tarea));

        when(estadoTareaRepository.findByNombre("Cancelada"))
                .thenReturn(Optional.of(cancelada));

        tareaService.cancelarTarea(
                10L,
                "Actividad ya no requerida",
                "coord@prueba.test");

        assertEquals(
                "Cancelada",
                tarea.getEstado().getNombre());

        verify(tareaRepository).save(tarea);

        verify(historialTareaService).registrarEvento(
                eq(tarea),
                eq(actor),
                eq("CANCELACION"),
                eq(Map.of("estado", "Pendiente")),
                eq(Map.of("estado", "Cancelada")),
                eq("Actividad ya no requerida"));
    }

    @Test
    void colaboradorNoPuedeCancelarTarea() {

        Rol rol = new Rol();
        rol.setNombre("COLABORADOR");

        Usuario actor = new Usuario();
        actor.setIdUsuario(2L);
        actor.setActivo(true);
        actor.setRol(rol);

        when(usuarioRepository.findByCorreo("colaborador@prueba.test"))
                .thenReturn(Optional.of(actor));

        assertThrows(
                AccessDeniedException.class,
                () -> tareaService.cancelarTarea(
                        10L,
                        "Motivo de prueba",
                        "colaborador@prueba.test")
        );

        verifyNoInteractions(
                tareaRepository,
                historialTareaService);
    }

    @Test
    void noPermiteCancelarTareasFinalizadas() {

        Rol rol = new Rol();
        rol.setNombre("COORDINADOR");

        Usuario actor = new Usuario();
        actor.setIdUsuario(1L);
        actor.setActivo(true);
        actor.setRol(rol);

        when(usuarioRepository.findByCorreo("coord@prueba.test"))
                .thenReturn(Optional.of(actor));

        for (String estado : List.of("Completada", "Cancelada")) {

            EstadoTarea estadoActual = new EstadoTarea();
            estadoActual.setNombre(estado);

            Tarea tarea = new Tarea();
            tarea.setIdTarea(10L);
            tarea.setEstado(estadoActual);

            when(tareaRepository.findByIdTarea(10L))
                    .thenReturn(Optional.of(tarea));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> tareaService.cancelarTarea(
                            10L,
                            "Motivo de prueba",
                            "coord@prueba.test")
            );

            assertEquals(estado, tarea.getEstado().getNombre());
        }

        verify(tareaRepository, never()).save(any(Tarea.class));
        verifyNoInteractions(historialTareaService);
    }

    @Test
    void noPermiteCancelarSinMotivo() {

        Rol rol = new Rol();
        rol.setNombre("COORDINADOR");

        Usuario actor = new Usuario();
        actor.setIdUsuario(1L);
        actor.setActivo(true);
        actor.setRol(rol);

        when(usuarioRepository.findByCorreo("coord@prueba.test"))
                .thenReturn(Optional.of(actor));

        assertThrows(
                IllegalArgumentException.class,
                () -> tareaService.cancelarTarea(
                        10L,
                        "   ",
                        "coord@prueba.test")
        );

        verifyNoInteractions(
                tareaRepository,
                historialTareaService);
    }

}
