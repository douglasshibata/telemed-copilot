package com.med.dto;

import com.med.domain.SoapStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapNotePayloadDto {
    private String subjective;
    private String objective;
    private String assessment;
    private SoapPlanDto plan;
    private String recommendations;
    private Integer followUpDays;
    private Double confidenceScore;
    private Integer rawTranscriptionLength;
    private SoapStatus status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoapPlanDto {
        private List<PrescriptionDto> prescriptions;
        private List<String> examsRequested;
        private String recommendations;
        private Integer followUpDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionDto {
        private String medication;
        private String dosage;
    }
}
