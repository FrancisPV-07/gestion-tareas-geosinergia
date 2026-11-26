package com.geosinergia.gestiontareas.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.geosinergia.gestiontareas.entity.HistorialTarea;
import com.geosinergia.gestiontareas.entity.Tarea;
import com.geosinergia.gestiontareas.entity.Usuario;
import com.geosinergia.gestiontareas.repository.HistorialTareaRepository;

@Service
public class HistorialTareaService {

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "EDICION",
            "REASIGNACION",
            "DESASIGNACION",
            "CANCELACION",
            "CAMBIO_ESTADO"
    );

    private final HistorialTareaRepository historialRepository;

    public HistorialTareaService(
            HistorialTareaRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEvento(
            Tarea tarea,
            Usuario usuarioActor,
            String tipoEvento,
            Map<String, Object> valorAnterior,
            Map<String, Object> valorNuevo,
            String motivo) {

        if (tarea == null || tarea.getIdTarea() == null) {
            throw new IllegalArgumentException(
                    "La tarea del historial no es valida.");
        }

        if (usuarioActor == null
                || usuarioActor.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El usuario del historial no es valido.");
        }

        if (!TIPOS_VALIDOS.contains(tipoEvento)) {
            throw new IllegalArgumentException(
                    "El tipo de evento no es valido.");
        }

        String motivoLimpio = motivo == null
                || motivo.isBlank() ? null : motivo.trim();

        boolean requiereMotivo =
                "REASIGNACION".equals(tipoEvento)
                || "DESASIGNACION".equals(tipoEvento)
                || "CANCELACION".equals(tipoEvento);

        if (requiereMotivo && motivoLimpio == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo de la operacion.");
        }

        HistorialTarea historial = new HistorialTarea();

        historial.setTarea(tarea);
        historial.setUsuarioActor(usuarioActor);
        historial.setTipoEvento(tipoEvento);
        historial.setValorAnterior(valorAnterior);
        historial.setValorNuevo(valorNuevo);
        historial.setMotivo(motivoLimpio);

        historialRepository.save(historial);
    }
}
