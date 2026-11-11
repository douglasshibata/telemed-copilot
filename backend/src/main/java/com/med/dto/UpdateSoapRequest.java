package com.med.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSoapRequest {
    private String subjective;
    private String objective;
    private String assessment;
    private String recommendations;
    private Integer followUpDays;
    private List<String> prescriptions;
    private List<String> examsRequested;
    private Boolean approveAndSign;
}
