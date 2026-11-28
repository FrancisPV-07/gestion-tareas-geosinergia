package com.geosinergia.gestiontareas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

import com.geosinergia.gestiontareas.entity.*;
import com.geosinergia.gestiontareas.repository.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceAsignacionHistorialTest {

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
    void asignacionInicialRegistraUnEventoConValoresCorrectos() {

        // Coordinador autenticado
        Rol rolCoordinador = new Rol();
        rolCoordinador.setNombre("COORDINADOR");

        Usuario coordinador = new Usuario();
        coordinador.setIdUsuario(1L);
        coordinador.setActivo(true);
        coordinador.setRol(rolCoordinador);

        // Colaborador activo
        Rol rolColaborador = new Rol();
        rolColaborador.setNombre("COLABORADOR");

        Usuario colaborador = new Usuario();
        colaborador.setIdUsuario(3L);
        colaborador.setActivo(true);
        colaborador.setRol(rolColaborador);

        // Prioridad
        Prioridad prioridad = new Prioridad();
        prioridad.setIdPrioridad(2L);

        // Estado inicial
        EstadoTarea pendiente = new EstadoTarea();
        pendiente.setNombre("Pendiente");

        // Tarea inicialmente sin asignar
        Tarea tarea = new Tarea();
        tarea.setIdTarea(10L);
        tarea.setEstado(pendiente);
        tarea.setResponsable(null);
        tarea.setPrioridad(null);
        tarea.setFechaLimite(null);

        LocalDate fechaLimite =
                LocalDate.now().plusDays(14);

        // Simular repositorios
        when(usuarioRepository.findByCorreo(
                "coord@prueba.test"))
                .thenReturn(Optional.of(coordinador));

        when(tareaRepository.findById(10L))
                .thenReturn(Optional.of(tarea));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(colaborador));

        when(prioridadRepository.findById(2L))
                .thenReturn(Optional.of(prioridad));

        when(tareaRepository.save(tarea))
                .thenReturn(tarea);

        // Ejecutar asignación
        Tarea resultado = tareaService.asignarTarea(
                10L,
                3L,
                2L,
                fechaLimite,
                "coord@prueba.test");

        // Validar resultado
        assertSame(colaborador, resultado.getResponsable());
        assertSame(prioridad, resultado.getPrioridad());
        assertEquals(fechaLimite, resultado.getFechaLimite());
        assertEquals("Pendiente",
                resultado.getEstado().getNombre());

        verify(tareaRepository, times(1)).save(tarea);

        // Valores anteriores esperados
        Map<String, Object> anterior =
                new LinkedHashMap<>();

        anterior.put("responsableId", null);
        anterior.put("prioridadId", null);
        anterior.put("fechaLimite", null);
        anterior.put("estado", "Pendiente");

        // Valores nuevos esperados
        Map<String, Object> nuevo =
                new LinkedHashMap<>();

        nuevo.put("responsableId", 3L);
        nuevo.put("prioridadId", 2L);
        nuevo.put("fechaLimite", fechaLimite.toString());
        nuevo.put("estado", "Pendiente");

        // Validar un único evento ASIGNACION
        verify(historialTareaService, times(1))
                .registrarEvento(
                        eq(tarea),
                        eq(coordinador),
                        eq("ASIGNACION"),
                        eq(anterior),
                        eq(nuevo),
                        isNull());

        verifyNoMoreInteractions(historialTareaService);
    }
}
