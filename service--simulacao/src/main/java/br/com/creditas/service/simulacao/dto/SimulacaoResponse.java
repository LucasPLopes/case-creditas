package br.com.creditas.service.simulacao.dto;

import java.math.BigDecimal;

public record SimulacaoResponse(
        BigDecimal valorTotal,
        BigDecimal parcelaMensal,
        BigDecimal totalJuros,
        BigDecimal taxaJurosAnual
) {}