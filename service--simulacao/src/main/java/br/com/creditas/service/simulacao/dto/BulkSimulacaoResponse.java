package br.com.creditas.service.simulacao.dto;

import java.util.List;

public record BulkSimulacaoResponse(
        String canal,
        String correlationId,
        List<SimulacaoResultado> responses
) {}