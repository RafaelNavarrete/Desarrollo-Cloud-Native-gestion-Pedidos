package com.duoc.gestion_pedidos.model;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa una guía de despacho almacenada en Oracle Cloud.
 * Se guarda en una tabla distinta a la usada en sumativas anteriores.
 *
 * @author Rafael Navarrete
 */

@Entity
@Table(name = "GUIAS_PROCESADAS")
public class GuiaDespachoOracle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "guias_seq")
    @SequenceGenerator(name = "guias_seq", sequenceName = "GUIAS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "GUIA_ID")
    private String guiaId;

    @Column(name = "TRANSPORTISTA")
    private String transportista;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "FECHA")
    private String fecha;

    public GuiaDespachoOracle() {}

    public GuiaDespachoOracle(String guiaId, String transportista, String descripcion, String fecha) {
        this.guiaId = guiaId;
        this.transportista = transportista;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public String getGuiaId() { return guiaId; }
    public String getTransportista() { return transportista; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }

    public void setId(Long id) { this.id = id; }
    public void setGuiaId(String guiaId) { this.guiaId = guiaId; }
    public void setTransportista(String transportista) { this.transportista = transportista; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}