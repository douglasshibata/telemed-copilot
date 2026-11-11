package com.med.service;

import com.med.config.RabbitMqConfig;
import com.med.domain.Consultation;
import com.med.domain.ConsultationStatus;
import com.med.domain.Doctor;
import com.med.domain.Patient;
import com.med.dto.CreateConsultationRequest;
import com.med.repository.ConsultationRepository;
import com.med.repository.DoctorRepository;
import com.med.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Consultation createConsultation(CreateConsultationRequest req) {
        Doctor doctor = null;
        if (req.getDoctorId() != null) {
            doctor = doctorRepository.findById(req.getDoctorId()).orElse(null);
        }
        if (doctor == null && req.getDoctorName() != null) {
            doctor = doctorRepository.save(Doctor.builder()
                    .name(req.getDoctorName())
                    .email(req.getDoctorEmail())
                    .crm(req.getDoctorCrm() != null ? req.getDoctorCrm() : "CRM/SP 123456")
                    .specialty("Psiquiatria")
                    .build());
        }

        Patient patient = null;
        if (req.getPatientId() != null) {
            patient = patientRepository.findById(req.getPatientId()).orElse(null);
        }
        if (patient == null && req.getPatientName() != null) {
            patient = patientRepository.save(Patient.builder()
                    .name(req.getPatientName())
                    .email(req.getPatientEmail())
                    .phone("+55 11 99999-8888")
                    .build());
        }

        String room = req.getRoomName() != null ? req.getRoomName() : "telemed-room-" + UUID.randomUUID().toString().substring(0, 8);

        Consultation consultation = Consultation.builder()
                .roomName(room)
                .doctor(doctor)
                .patient(patient)
                .status(ConsultationStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .scheduledAt(LocalDateTime.now())
                .build();

        return consultationRepository.save(consultation);
    }

    public Consultation getConsultation(String id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found: " + id));
    }

    @Transactional
    public void notifyAudioReady(String consultationId, String audioStorageKey) {
        Consultation consultation = getConsultation(consultationId);
        consultation.setAudioStorageKey(audioStorageKey);
        consultation.setAudioUploadedAt(LocalDateTime.now());
        consultation.setStatus(ConsultationStatus.AUDIO_PROCESSING);
        consultationRepository.save(consultation);

        Map<String, Object> message = new HashMap<>();
        message.put("consultation_id", consultationId);
        message.put("audio_storage_key", audioStorageKey);
        message.put("timestamp", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY_AUDIO_READY, message);
        log.info("Dispatched audio.ready event to RabbitMQ for consultation: {}", consultationId);
    }
}
