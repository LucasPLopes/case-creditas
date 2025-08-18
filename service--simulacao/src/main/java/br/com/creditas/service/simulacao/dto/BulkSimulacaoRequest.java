package br.com.creditas.service.simulacao.dto;

import java.util.List;


public record BulkSimulacaoRequest(
        String canalOrigem,
        String correlationId,
        List<SimulacaoSolicitacao> simulacoes
) { }

