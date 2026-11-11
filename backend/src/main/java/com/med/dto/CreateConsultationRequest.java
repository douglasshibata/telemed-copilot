package com.med.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationRequest {
    private String roomName;
    private String doctorId;
    private String patientId;
    private String doctorName;
    private String doctorEmail;
    private String doctorCrm;
    private String patientName;
    private String patientEmail;
}
