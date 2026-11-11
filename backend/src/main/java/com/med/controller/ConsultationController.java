package com.med.controller;

import com.med.domain.Consultation;
import com.med.domain.SoapNote;
import com.med.dto.CreateConsultationRequest;
import com.med.dto.JitsiTokenResponse;
import com.med.dto.UpdateSoapRequest;
import com.med.service.ConsultationService;
import com.med.service.JitsiTokenService;
import com.med.service.SoapNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final SoapNoteService soapNoteService;
    private final JitsiTokenService jitsiTokenService;

    @PostMapping
    public ResponseEntity<Consultation> createConsultation(@RequestBody CreateConsultationRequest request) {
        Consultation consultation = consultationService.createConsultation(request);
        return ResponseEntity.ok(consultation);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consultation> getConsultation(@PathVariable String id) {
        return ResponseEntity.ok(consultationService.getConsultation(id));
    }

    @PostMapping("/{id}/token")
    public ResponseEntity<JitsiTokenResponse> generateJitsiToken(
            @PathVariable String id,
            @RequestParam(defaultValue = "true") boolean isDoctor,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String userName) {

        Consultation consultation = consultationService.getConsultation(id);
        String name = userName != null ? userName : (isDoctor ? "Dr(a). Médico" : "Paciente");
        String uid = userId != null ? userId : (isDoctor ? "doc-1" : "pat-1");
        String email = isDoctor ? "doctor@telemed.com" : "patient@telemed.com";

        String token = jitsiTokenService.generateToken(
                consultation.getRoomName(),
                uid,
                name,
                email,
                isDoctor
        );

        return ResponseEntity.ok(JitsiTokenResponse.builder()
                .token(token)
                .room(consultation.getRoomName())
                .domain(jitsiTokenService.getDomain())
                .isDoctor(isDoctor)
                .build());
    }

    @PutMapping("/{id}/soap")
    public ResponseEntity<SoapNote> updateSoap(
            @PathVariable String id,
            @RequestBody UpdateSoapRequest request,
            @RequestParam(defaultValue = "doc-1") String doctorId) {

        SoapNote updated = soapNoteService.updateSoapNote(id, request, doctorId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/audio-ready")
    public ResponseEntity<Map<String, String>> notifyAudioReady(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {

        String storageKey = payload.getOrDefault("audio_storage_key", "consultation-" + id + ".wav");
        consultationService.notifyAudioReady(id, storageKey);
        return ResponseEntity.ok(Map.of("status", "AUDIO_PROCESSING_INITIATED", "consultationId", id));
    }
}
