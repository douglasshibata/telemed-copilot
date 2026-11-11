package com.med.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JitsiTokenResponse {
    private String token;
    private String room;
    private String domain;
    @com.fasterxml.jackson.annotation.JsonProperty("isDoctor")
    private boolean isDoctor;
}
