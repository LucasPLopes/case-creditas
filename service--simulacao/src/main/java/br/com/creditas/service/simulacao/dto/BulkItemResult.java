package br.com.creditas.service.simulacao.dto;


public record BulkItemResult(
        SimulacaoResultado response,
        String erro
) {}
