package com.med;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.med.domain.Consultation;
import com.med.domain.ConsultationStatus;
import com.med.domain.Doctor;
import com.med.domain.Patient;
import com.med.domain.SoapNote;
import com.med.domain.SoapStatus;
import com.med.dto.CreateConsultationRequest;
import com.med.dto.UpdateSoapRequest;
import com.med.service.ConsultationService;
import com.med.service.JitsiTokenService;
import com.med.service.SoapNoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConsultationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultationService consultationService;

    @MockBean
    private SoapNoteService soapNoteService;

    @MockBean
    private JitsiTokenService jitsiTokenService;

    @Test
    void testCreateConsultation() throws Exception {
        Consultation consultation = Consultation.builder()
                .id("c-100")
                .roomName("room-abc")
                .doctor(Doctor.builder().id("d-1").name("Dr. House").build())
                .patient(Patient.builder().id("p-1").name("John Doe").build())
                .status(ConsultationStatus.IN_PROGRESS)
                .build();

        when(consultationService.createConsultation(any())).thenReturn(consultation);

        CreateConsultationRequest req = CreateConsultationRequest.builder()
                .doctorName("Dr. House")
                .patientName("John Doe")
                .build();

        mockMvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c-100"))
                .andExpect(jsonPath("$.roomName").value("room-abc"))
                .andExpect(jsonPath("$.doctor.name").value("Dr. House"));
    }

    @Test
    void testGenerateJitsiToken() throws Exception {
        Consultation consultation = Consultation.builder()
                .id("c-100")
                .roomName("room-abc")
                .build();

        when(consultationService.getConsultation("c-100")).thenReturn(consultation);
        when(jitsiTokenService.generateToken(eq("room-abc"), any(), any(), any(), eq(true))).thenReturn("mock-jwt-token");
        when(jitsiTokenService.getDomain()).thenReturn("meet.jit.si");

        mockMvc.perform(post("/api/consultations/c-100/token?isDoctor=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.room").value("room-abc"))
                .andExpect(jsonPath("$.isDoctor").value(true));
    }

    @Test
    void testUpdateSoapNote() throws Exception {
        SoapNote note = SoapNote.builder()
                .id("soap-1")
                .consultationId("c-100")
                .subjective("Sintomas controlados")
                .status(SoapStatus.APPROVED_AND_SIGNED)
                .build();

        when(soapNoteService.updateSoapNote(eq("c-100"), any(), eq("doc-1"))).thenReturn(note);

        UpdateSoapRequest req = UpdateSoapRequest.builder()
                .subjective("Sintomas controlados")
                .approveAndSign(true)
                .build();

        mockMvc.perform(put("/api/consultations/c-100/soap?doctorId=doc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED_AND_SIGNED"))
                .andExpect(jsonPath("$.subjective").value("Sintomas controlados"));
    }
}
