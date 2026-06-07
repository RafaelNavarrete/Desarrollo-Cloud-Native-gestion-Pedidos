package com.duoc.gestion_pedidos.model;

/**
 * Clase modelo que representa una guía de despacho del sistema de transporte.
 * Contiene los datos básicos de la guía, incluyendo identificador,
 * transportista asociado, descripción del pedido y fecha de emisión.
 *
 * @author Rafael Navarrete
 */
public class GuiaDespacho {

    private String id;
    private String transportista;
    private String descripcion;
    private String fecha; // formato ddMMyyyy, ej: 06062026

    public GuiaDespacho() {}

    public GuiaDespacho(String id, String transportista, String descripcion, String fecha) {
        this.id = id;
        this.transportista = transportista;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public String getTransportista() { return transportista; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }

    public void setId(String id) { this.id = id; }
    public void setTransportista(String transportista) { this.transportista = transportista; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}