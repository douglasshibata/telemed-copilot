package com.med.listener;

import com.med.dto.SoapGeneratedEventDto;
import com.med.service.SoapNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoapGeneratedListener {

    private final SoapNoteService soapNoteService;

    @RabbitListener(queues = "consultation.soap.generated")
    public void onSoapGenerated(SoapGeneratedEventDto event) {
        log.info("Received consultation.soap.generated for consultation_id: {}", event.getConsultationId());
        try {
            soapNoteService.processSoapGeneratedEvent(event);
        } catch (Exception e) {
            log.error("Failed to process SOAP generated event: {}", e.getMessage(), e);
        }
    }
}
