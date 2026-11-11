package com.med.dto;

import com.med.dto.SoapNotePayloadDto.SoapPlanDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapGeneratedEventDto {

    @JsonProperty("consultation_id")
    private String consultationId;

    @JsonProperty("soap_note")
    private SoapNoteContent soapNote;

    @JsonProperty("raw_transcription_length")
    private Integer rawTranscriptionLength;

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoapNoteContent {
        private String subjective;
        private String objective;
        private String assessment;
        private SoapPlanDto plan;
    }
}
