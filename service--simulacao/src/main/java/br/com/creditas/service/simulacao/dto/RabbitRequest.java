package br.com.creditas.service.simulacao.dto;

public record RabbitRequest(
        String chaveComposta,
        int index,
        SimulacaoSolicitacao request
) {}