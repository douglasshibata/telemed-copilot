package com.med.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "telemed.exchange";
    public static final String QUEUE_AUDIO_READY = "consultation.audio.ready";
    public static final String QUEUE_SOAP_GENERATED = "consultation.soap.generated";
    public static final String ROUTING_KEY_AUDIO_READY = "audio.ready";
    public static final String ROUTING_KEY_SOAP_GENERATED = "soap.generated";

    @Bean
    public DirectExchange telemedExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue audioReadyQueue() {
        return new Queue(QUEUE_AUDIO_READY, true);
    }

    @Bean
    public Queue soapGeneratedQueue() {
        return new Queue(QUEUE_SOAP_GENERATED, true);
    }

    @Bean
    public Binding bindingAudioReady(Queue audioReadyQueue, DirectExchange telemedExchange) {
        return BindingBuilder.bind(audioReadyQueue).to(telemedExchange).with(ROUTING_KEY_AUDIO_READY);
    }

    @Bean
    public Binding bindingSoapGenerated(Queue soapGeneratedQueue, DirectExchange telemedExchange) {
        return BindingBuilder.bind(soapGeneratedQueue).to(telemedExchange).with(ROUTING_KEY_SOAP_GENERATED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
