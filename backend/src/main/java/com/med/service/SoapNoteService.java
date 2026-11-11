package com.med.service;

import com.med.domain.Consultation;
import com.med.domain.ConsultationStatus;
import com.med.domain.SoapNote;
import com.med.domain.SoapStatus;
import com.med.dto.SoapGeneratedEventDto;
import com.med.dto.UpdateSoapRequest;
import com.med.repository.ConsultationRepository;
import com.med.repository.SoapNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoapNoteService {

    private final SoapNoteRepository soapNoteRepository;
    private final ConsultationRepository consultationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public SoapNote processSoapGeneratedEvent(SoapGeneratedEventDto event) {
        log.info("Processing generated SOAP for consultation: {}", event.getConsultationId());

        Consultation consultation = consultationRepository.findById(event.getConsultationId())
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found: " + event.getConsultationId()));

        SoapNote note = soapNoteRepository.findByConsultationId(event.getConsultationId())
                .orElse(SoapNote.builder()
                        .consultationId(event.getConsultationId())
                        .createdAt(LocalDateTime.now())
                        .build());

        var content = event.getSoapNote();
        if (content != null) {
            note.setSubjective(content.getSubjective());
            note.setObjective(content.getObjective());
            note.setAssessment(content.getAssessment());

            if (content.getPlan() != null) {
                note.setRecommendations(content.getPlan().getRecommendations());
                note.setFollowUpDays(content.getPlan().getFollowUpDays());

                if (content.getPlan().getPrescriptions() != null) {
                    List<String> prescriptionsFormatted = content.getPlan().getPrescriptions().stream()
                            .map(p -> p.getMedication() + " - " + p.getDosage())
                            .collect(Collectors.toList());
                    note.setPrescriptions(prescriptionsFormatted);
                }

                if (content.getPlan().getExamsRequested() != null) {
                    note.setExamsRequested(new ArrayList<>(content.getPlan().getExamsRequested()));
                }
            }
        }

        note.setConfidenceScore(event.getConfidenceScore());
        note.setRawTranscriptionLength(event.getRawTranscriptionLength());
        note.setStatus(SoapStatus.DRAFT);
        note.setUpdatedAt(LocalDateTime.now());

        SoapNote savedNote = soapNoteRepository.save(note);

        consultation.setSoapNote(savedNote);
        consultation.setStatus(ConsultationStatus.PENDING_DOCTOR_APPROVAL);
        consultationRepository.save(consultation);

        // Push real-time notification to the doctor's frontend via STOMP WebSocket
        messagingTemplate.convertAndSend("/topic/consultation/" + consultation.getId() + "/soap", savedNote);
        log.info("SOAP note broadcasted via WebSocket to /topic/consultation/{}/soap", consultation.getId());

        return savedNote;
    }

    @Transactional
    public SoapNote updateSoapNote(String consultationId, UpdateSoapRequest request, String doctorId) {
        SoapNote note = soapNoteRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("SOAP Note not found for consultation: " + consultationId));

        if (request.getSubjective() != null) note.setSubjective(request.getSubjective());
        if (request.getObjective() != null) note.setObjective(request.getObjective());
        if (request.getAssessment() != null) note.setAssessment(request.getAssessment());
        if (request.getRecommendations() != null) note.setRecommendations(request.getRecommendations());
        if (request.getFollowUpDays() != null) note.setFollowUpDays(request.getFollowUpDays());
        if (request.getPrescriptions() != null) note.setPrescriptions(request.getPrescriptions());
        if (request.getExamsRequested() != null) note.setExamsRequested(request.getExamsRequested());

        if (Boolean.TRUE.equals(request.getApproveAndSign())) {
            note.setStatus(SoapStatus.APPROVED_AND_SIGNED);
            note.setSignedByDoctorId(doctorId);
            note.setSignedAt(LocalDateTime.now());

            Consultation consultation = consultationRepository.findById(consultationId).orElse(null);
            if (consultation != null) {
                consultation.setStatus(ConsultationStatus.FINALIZED);
                // Purge original audio reference to comply with LGPD TTL policies
                consultation.setAudioPurgedAt(LocalDateTime.now());
                consultationRepository.save(consultation);
            }
        }

        note.setUpdatedAt(LocalDateTime.now());
        SoapNote savedNote = soapNoteRepository.save(note);

        messagingTemplate.convertAndSend("/topic/consultation/" + consultationId + "/soap", savedNote);
        return savedNote;
    }
}
