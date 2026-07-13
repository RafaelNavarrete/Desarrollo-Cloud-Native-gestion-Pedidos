package com.duoc.gestion_pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ.
 * Define dos colas:
 * - guias_queue: cola principal donde se envían las guías de despacho
 * - guias_error_queue: Dead Letter Queue donde van los mensajes que fallan
 *
 * @author Rafael Navarrete
 */

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_GUIAS = "guias_queue";
    public static final String QUEUE_ERROR = "guias_error_queue";
    public static final String EXCHANGE_GUIAS = "guias_exchange";
    public static final String EXCHANGE_ERROR = "guias_error_exchange";
    public static final String ROUTING_KEY_GUIAS = "guias.key";
    public static final String ROUTING_KEY_ERROR = "guias.error.key";

    // Cola principal con DLX configurado
    @Bean
    public Queue guiasQueue() {
        return QueueBuilder.durable(QUEUE_GUIAS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_ERROR)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_ERROR)
                .build();
    }

    // Cola de error (Dead Letter Queue)
    @Bean
    public Queue guiasErrorQueue() {
        return QueueBuilder.durable(QUEUE_ERROR).build();
    }

    // Exchange principal
    @Bean
    public DirectExchange guiasExchange() {
        return new DirectExchange(EXCHANGE_GUIAS);
    }

    // Exchange de error
    @Bean
    public DirectExchange guiasErrorExchange() {
        return new DirectExchange(EXCHANGE_ERROR);
    }

    // Binding entre la cola principal y el exchange principal
    @Bean
    public Binding bindingGuias() {
        return BindingBuilder.bind(guiasQueue())
                .to(guiasExchange())
                .with(ROUTING_KEY_GUIAS);
    }

    // Binding cola errores con exchange de errores
    @Bean
    public Binding bindingGuiasError() {
        return BindingBuilder.bind(guiasErrorQueue())
                .to(guiasErrorExchange())
                .with(ROUTING_KEY_ERROR);
    }
    
}