package com.duoc.gestion_pedidos.services;

import com.duoc.gestion_pedidos.config.RabbitMQConfig;
import com.duoc.gestion_pedidos.model.GuiaDespachoOracle;
import com.duoc.gestion_pedidos.repository.GuiaDespachoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Servicio consumidor que lee mensajes de la cola principal
 * y los persiste en Oracle Cloud.
 *
 * @author Rafael Navarrete
 */
@Service
public class GuiaConsumidorService {

    private final GuiaDespachoRepository repository;

    public GuiaConsumidorService(GuiaDespachoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GUIAS)
    public void consumirGuia(String mensaje) {
        try {
            System.out.println("Mensaje recibido de la cola: " + mensaje);
            String[] partes = mensaje.split("\\|");
            GuiaDespachoOracle guia = new GuiaDespachoOracle(
                partes[0], // guiaId
                partes[1], // transportista
                partes[2], // descripcion
                partes[3]  // fecha
            );
            repository.save(guia);
            System.out.println("Guía guardada en Oracle: " + partes[0]);
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje: " + e.getMessage());
            throw new RuntimeException(e); // lanza excepción para que vaya a la DLQ
        }
    }
}