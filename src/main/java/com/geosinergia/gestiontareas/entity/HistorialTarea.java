package com.geosinergia.gestiontareas.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "historial_tarea")
public class HistorialTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tarea", nullable = false)
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_actor", nullable = false)
    private Usuario usuarioActor;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "tipo_evento", length = 30, nullable = false)
    private String tipoEvento;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_anterior", columnDefinition = "json")
    private Map<String, Object> valorAnterior;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_nuevo", columnDefinition = "json")
    private Map<String, Object> valorNuevo;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @PrePersist
    public void registrarFechaEvento() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }

    public Long getIdHistorial() {
        return idHistorial;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    public Usuario getUsuarioActor() {
        return usuarioActor;
    }

    public void setUsuarioActor(Usuario usuarioActor) {
        this.usuarioActor = usuarioActor;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Map<String, Object> getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(Map<String, Object> valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public Map<String, Object> getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(Map<String, Object> valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
