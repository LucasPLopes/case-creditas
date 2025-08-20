package br.com.creditas.service.proposta.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SimulacaoResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String id,
        SimulacaoResultado resultado
) { }