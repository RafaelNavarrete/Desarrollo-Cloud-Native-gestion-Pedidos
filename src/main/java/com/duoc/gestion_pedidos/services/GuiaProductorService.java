package com.duoc.gestion_pedidos.services;

import com.duoc.gestion_pedidos.config.RabbitMQConfig;
import com.duoc.gestion_pedidos.model.GuiaDespacho;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio productor que envía mensajes de guías de despacho
 * a la cola principal de RabbitMQ.
 *
 * @author Rafael Navarrete
 */

@Service
public class GuiaProductorService {

    private final RabbitTemplate rabbitTemplate;

    public GuiaProductorService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarGuia(GuiaDespacho guia) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_GUIAS,
                RabbitMQConfig.ROUTING_KEY_GUIAS,
                guia.getId() + "|" + guia.getTransportista() + "|" + guia.getDescripcion() + "|" + guia.getFecha()
            );
            System.out.println("Guía enviada a la cola: " + guia.getId());
        } catch (Exception e) {
            System.err.println("Error al enviar guía a cola: " + e.getMessage());
            throw e;
        }
    }
    
}