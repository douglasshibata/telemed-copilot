package com.med;

import com.med.domain.Consultation;
import com.med.domain.ConsultationStatus;
import com.med.domain.SoapNote;
import com.med.domain.SoapStatus;
import com.med.dto.SoapGeneratedEventDto;
import com.med.dto.SoapNotePayloadDto.PrescriptionDto;
import com.med.dto.SoapNotePayloadDto.SoapPlanDto;
import com.med.dto.UpdateSoapRequest;
import com.med.repository.ConsultationRepository;
import com.med.repository.SoapNoteRepository;
import com.med.service.SoapNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoapNoteServiceTest {

    @Mock
    private SoapNoteRepository soapNoteRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private SoapNoteService soapNoteService;

    private Consultation consultation;

    @BeforeEach
    void setUp() {
        consultation = Consultation.builder()
                .id("consult-123")
                .roomName("test-room")
                .status(ConsultationStatus.AUDIO_PROCESSING)
                .build();
    }

    @Test
    void testProcessSoapGeneratedEvent() {
        when(consultationRepository.findById("consult-123")).thenReturn(Optional.of(consultation));
        when(soapNoteRepository.findByConsultationId("consult-123")).thenReturn(Optional.empty());
        when(soapNoteRepository.save(any(SoapNote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SoapGeneratedEventDto event = SoapGeneratedEventDto.builder()
                .consultationId("consult-123")
                .rawTranscriptionLength(1200)
                .confidenceScore(0.95)
                .soapNote(SoapGeneratedEventDto.SoapNoteContent.builder()
                        .subjective("Paciente relata cefaleia constante.")
                        .objective("Pressão arterial 120/80 mmHg.")
                        .assessment("Cefaleia tensional.")
                        .plan(SoapPlanDto.builder()
                                .prescriptions(List.of(new PrescriptionDto("Dipirona", "500mg 6/6h")))
                                .examsRequested(List.of("Ressonância magnética"))
                                .recommendations("Repouso e hidratação")
                                .followUpDays(15)
                                .build())
                        .build())
                .build();

        SoapNote note = soapNoteService.processSoapGeneratedEvent(event);

        assertNotNull(note);
        assertEquals(SoapStatus.DRAFT, note.getStatus());
        assertEquals("Paciente relata cefaleia constante.", note.getSubjective());
        assertEquals(1, note.getPrescriptions().size());
        assertEquals("Dipirona - 500mg 6/6h", note.getPrescriptions().get(0));
        assertEquals(ConsultationStatus.PENDING_DOCTOR_APPROVAL, consultation.getStatus());

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/consultation/consult-123/soap"), eq(note));
    }

    @Test
    void testUpdateAndApproveSoapNote() {
        SoapNote existing = SoapNote.builder()
                .id("soap-1")
                .consultationId("consult-123")
                .subjective("Draft")
                .status(SoapStatus.DRAFT)
                .build();

        when(soapNoteRepository.findByConsultationId("consult-123")).thenReturn(Optional.of(existing));
        when(soapNoteRepository.save(any(SoapNote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(consultationRepository.findById("consult-123")).thenReturn(Optional.of(consultation));

        UpdateSoapRequest req = UpdateSoapRequest.builder()
                .subjective("Paciente revisado com sucesso.")
                .approveAndSign(true)
                .build();

        SoapNote updated = soapNoteService.updateSoapNote("consult-123", req, "doc-999");

        assertEquals(SoapStatus.APPROVED_AND_SIGNED, updated.getStatus());
        assertEquals("doc-999", updated.getSignedByDoctorId());
        assertNotNull(updated.getSignedAt());
        assertEquals(ConsultationStatus.FINALIZED, consultation.getStatus());
        assertNotNull(consultation.getAudioPurgedAt());
    }
}
