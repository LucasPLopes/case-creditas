package br.com.creditas.service.simulacao.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record SimulacaoResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String id,
        SimulacaoResultado resultado
) { }