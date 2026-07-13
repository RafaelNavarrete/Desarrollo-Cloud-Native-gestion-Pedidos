package com.duoc.gestion_pedidos.controller;

import com.duoc.gestion_pedidos.model.GuiaDespacho;
import com.duoc.gestion_pedidos.services.EfsServices;
import com.duoc.gestion_pedidos.services.GuiaProductorService;
import com.duoc.gestion_pedidos.services.S3Services;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints del sistema de gestión
 * de pedidos y guías de despacho.
 * Coordina las operaciones entre EFS, S3, RabbitMQ y Oracle Cloud.
 *
 * @author Rafael Navarrete
 */

@RestController
@RequestMapping("/guias")
public class GuiaDespachoController {

    private final EfsServices efsServices;
    private final S3Services s3Services;
    private final GuiaProductorService productorService;

    public GuiaDespachoController(EfsServices efsServices, S3Services s3Services, GuiaProductorService productorService) {
        this.efsServices = efsServices;
        this.s3Services = s3Services;
        this.productorService = productorService;
    }

    // POST /guias - Crea la guia, la sube a S3 y envía mensaje a la cola
    @PostMapping
    public ResponseEntity<String> crearGuia(@RequestBody GuiaDespacho guia) {
        // Enviar a cola RabbitMQ siempre, independiente de S3
        productorService.enviarGuia(guia);
        
        try {
            String rutaEfs = efsServices.guardarGuiaEnEfs(guia);
            String nombreArchivo = "guia_" + guia.getId() + ".pdf";
            String keyS3 = s3Services.subirArchivo(rutaEfs, guia.getFecha(), guia.getTransportista(), nombreArchivo);
            efsServices.eliminarArchivoTemporal(rutaEfs);
            return ResponseEntity.ok("Guía creada, almacenada en S3 y enviada a cola: " + keyS3);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error S3 (mensaje enviado a cola igualmente): " + e.getMessage());
        }
    }

    // GET /guias/descargar - Descarga una guia desde S3
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

    // PUT /guias - Modifica una guia existente en S3 y notifica a la cola
    @PutMapping
    public ResponseEntity<String> actualizarGuia(@RequestBody GuiaDespacho guia) {
        try {
            String rutaEfs = efsServices.guardarGuiaEnEfs(guia);
            String nombreArchivo = "guia_" + guia.getId() + ".pdf";
            String keyS3 = s3Services.actualizarArchivo(rutaEfs, guia.getFecha(), guia.getTransportista(), nombreArchivo);
            efsServices.eliminarArchivoTemporal(rutaEfs);
            productorService.enviarGuia(guia);
            return ResponseEntity.ok("Guia actualizada en S3 y notificada a cola: " + keyS3);
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

    // GET /guias/consumir - Consume mensajes de la cola y los guarda en Oracle
    @GetMapping("/consumir")
    public ResponseEntity<String> consumirMensajes() {
        return ResponseEntity.ok("Consumidor activo. Los mensajes de la cola se procesan automáticamente y se guardan en Oracle Cloud.");
    }
}