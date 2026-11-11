package com.med.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "soap_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapNote {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String consultationId;

    @Lob
    private String subjective;

    @Lob
    private String objective;

    @Lob
    private String assessment;

    @Lob
    private String recommendations;

    private Integer followUpDays;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> prescriptions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> examsRequested = new ArrayList<>();

    private Double confidenceScore;

    private Integer rawTranscriptionLength;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SoapStatus status = SoapStatus.DRAFT;

    private String signedByDoctorId;

    private LocalDateTime signedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
