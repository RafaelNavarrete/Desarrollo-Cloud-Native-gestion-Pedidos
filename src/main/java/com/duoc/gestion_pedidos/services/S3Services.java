package com.duoc.gestion_pedidos.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona las operaciones de almacenamiento en AWS S3.
 * Permite subir, descargar, modificar, eliminar y consultar
 * las guías de despacho organizadas por fecha y transportista.
 *
 * @author Rafael Navarrete
 */

@Service
public class S3Services {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    // Construye el cliente S3 utilizando las credenciales de la instancia EC2
    private S3Client getClient() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    // Construye la ruta en la carpeta en S3: /fecha/transportista/
    private String buildKey(String fecha, String transportista, String nombreArchivo) {
        return Paths.get(fecha, transportista, nombreArchivo).toString();
    }

    // Suber el archivo desde EFS a S3
    public String subirArchivo(String rutaLocal, String fecha, String transportista, String nombreArchivo) {
        try {
            S3Client s3 = getClient();
            String key = buildKey(fecha, transportista, nombreArchivo);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            s3.putObject(request, Paths.get(rutaLocal));
            return key; // Retorna la clave del objeto en S3

        } catch (Exception e) {
            throw new RuntimeException("Error al subir el archivo a S3: " + e.getMessage(), e);
        }
    }

    // Descarga un archivo desde S3 y retorna su contenido como bytes
    public byte[] descargarArchivo(String fecha, String transportista, String nombreArchivo) {
        try {
            S3Client s3 = getClient();
            String key = buildKey(fecha, transportista, nombreArchivo);

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            InputStream stream = s3.getObject(request);
            return stream.readAllBytes();

        } catch (Exception e) {
            throw new RuntimeException("Error al descargar archivo de S3: " + e.getMessage());
        }
    }

    // Reemplaza un archivo existente en S3 con uno nueva versión
    public String actualizarArchivo(String rutaLocal, String fecha, String transportista, String nombreArchivo) {
        // En S3 actualizar es subir con la misma clave
        return subirArchivo(rutaLocal, fecha, transportista, nombreArchivo);
    }

    // Elimina un archivo específico de S3
    public void eliminarArchivo(String fecha, String transportista, String nombreArchivo) {
        try {
            S3Client s3 = getClient();
            String key = buildKey(fecha, transportista, nombreArchivo);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3.deleteObject(request);

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar archivo de S3: " + e.getMessage());
        }
    }

    // Consulta las guias de un transportista en una fecha específica
    public List<String> consultarGuias(String fecha, String transportista) {
        try {
            S3Client s3 = getClient();
            String prefijo = fecha + "/" + transportista + "/";

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefijo)
                    .build();

            ListObjectsV2Response response = s3.listObjectsV2(request);

            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar guías en S3: " + e.getMessage());
        }
    }
}