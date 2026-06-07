package com.duoc.gestion_pedidos.controller;

import com.duoc.gestion_pedidos.model.GuiaDespacho;
import com.duoc.gestion_pedidos.services.EfsServices;
import com.duoc.gestion_pedidos.services.S3Services;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints del sistema de gestión
 * de pedidos y guías de despacho.
 * Coordina las operaciones entre el almacenamiento temporal EFS
 * y el almacenamiento definitivo en AWS S3.
 *
 * @author Rafael Navarrete
 */

@RestController
@RequestMapping("/guias")
public class GuiaDespachoController {

    private final EfsServices efsServices;
    private final S3Services s3Services;

    public GuiaDespachoController(EfsServices efsServices, S3Services s3Services) {
        this.efsServices = efsServices;
        this.s3Services = s3Services;
    }

    // POST /guias - Crea la guia, la guarda en EFS y luego la sube a S3
    @PostMapping
    public ResponseEntity<String> crearGuia(@RequestBody GuiaDespacho guia) {
        try {
            // Paso 1: Guardar la guía en EFS
            String rutaEfs = efsServices.guardarGuiaEnEfs(guia);

            // Paso 2: Subir el archivo desde EFS a S3
            String nombreArchivo = "guia_" + guia.getId() + ".pdf";
            String keyS3 = s3Services.subirArchivo(rutaEfs, guia.getFecha(), guia.getTransportista(), nombreArchivo);

            // Paso 3: eliminar el archivo temporal de EFS
            efsServices.eliminarArchivoTemporal(rutaEfs);

            return ResponseEntity.ok("Guía creada y almacenada en S3 con clave: " + keyS3);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /// GET /guias/descargar - Descarga una guia desde S3
    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargarGuia(
            @RequestParam String fecha,
            @RequestParam String transportista,
            @RequestParam String nombreArchivo) {
        try {
            byte[] contenido = s3Services.descargarArchivo(fecha, transportista, nombreArchivo);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(contenido);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // PUT /guias - Modifica una guia existente en S3
    @PutMapping
    public ResponseEntity<String> actualizarGuia(@RequestBody GuiaDespacho guia) {
        try {
            // Regenerar PDF en EFS con los nuevos datos
            String rutaEfs = efsServices.guardarGuiaEnEfs(guia);

            // Reemplazar en S3
            String nombreArchivo = "guia_" + guia.getId() + ".pdf";
            String keyS3 = s3Services.actualizarArchivo(rutaEfs, guia.getFecha(), guia.getTransportista(), nombreArchivo);

            // Eliminar temporal
            efsServices.eliminarArchivoTemporal(rutaEfs);

            return ResponseEntity.ok("Guia actualizada en S3: " + keyS3);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // DELETE /guias - Elimina una guia especifica de S3
    @DeleteMapping
    public ResponseEntity<String> eliminarGuia(
            @RequestParam String fecha,
            @RequestParam String transportista,
            @RequestParam String nombreArchivo) {
        try {
            s3Services.eliminarArchivo(fecha, transportista, nombreArchivo);
            return ResponseEntity.ok("Guia eliminada: " + nombreArchivo);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // GET /guias/historial - Consulta guias por transportista y fecha
    @GetMapping("/historial")
    public ResponseEntity<List<String>> consultarHistorial(
            @RequestParam String fecha,
            @RequestParam String transportista) {
        try {
            List<String> guias = s3Services.consultarGuias(fecha, transportista);
            return ResponseEntity.ok(guias);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
}