package br.com.creditas.service.simulacao.dto;


public record BulkItemResult(
        SimulacaoResponse response,
        String erro
) {}
