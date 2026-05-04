package com.rednorte.sigle.citas_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "sigle.exchange";
    public static final String CANCELACION_QUEUE = "sigle.citas.canceladas";
    public static final String CANCELACION_ROUTING_KEY = "citas.cancelada";

    @Bean
    public Queue cancelacionQueue() {
        return new Queue(CANCELACION_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue cancelacionQueue, DirectExchange exchange) {
        return BindingBuilder.bind(cancelacionQueue).to(exchange).with(CANCELACION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
