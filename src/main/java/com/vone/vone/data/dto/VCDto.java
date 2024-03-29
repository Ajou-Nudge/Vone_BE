package com.vone.vone.data.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class VCDto {
    private String context;
    private String issuer;
    private Map<String, String> credentialSubject;
}
