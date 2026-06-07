package com.duoc.gestion_pedidos.services;

import com.duoc.gestion_pedidos.GestionPedidosApplication;
import com.duoc.gestion_pedidos.model.GuiaDespacho;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio que gestiona el almacenamiento temporal de guías de despacho
 * en el sistema de archivos EFS montado en el contenedor.
 * Genera el archivo PDF de la guía y lo guarda en la ruta configurada.
 *
 * @author Rafael Navarrete
 */

@Service
public class EfsServices {

    private final GestionPedidosApplication gestionPedidosApplication;
    
    @Value("${efs.path}")
    private String efsPath;

    EfsServices(GestionPedidosApplication gestionPedidosApplication) {
        this.gestionPedidosApplication = gestionPedidosApplication;
    }

    // Genera el PDF de la guía de despacho y lo guarda en EFS
    public String guardarGuiaEnEfs(GuiaDespacho guia) {
        try {
            // Crear carpeta si no existe
            Path carpeta = Paths.get(efsPath);
            if (!Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }

            String nombreArchivo = "guia_" + guia.getId() + ".pdf";
            String rutaCompleta = efsPath + File.separator + nombreArchivo;

            // Generar PDF con iText
            Document documento = new Document();
            PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));
            documento.open();
            documento.add(new Paragraph("Guía de Despacho"));
            documento.add(new Paragraph("ID: " + guia.getId()));
            documento.add(new Paragraph("Transportista: " + guia.getTransportista()));
            documento.add(new Paragraph("Fecha: " + guia.getFecha()));
            documento.close();

            return rutaCompleta; // Retorna la ruta del archivo guardado en EFS

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la guía en EFS: " + e.getMessage(), e);
        }
    }

    // Elimina el archivo temporal del EFS después de subirlo a S3
    public void eliminarArchivoTemporal(String rutaArchivo) {
        try {
            Files.deleteIfExists(Paths.get(rutaArchivo));
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el archivo temporal: " + e.getMessage(), e);
        }
    }

}