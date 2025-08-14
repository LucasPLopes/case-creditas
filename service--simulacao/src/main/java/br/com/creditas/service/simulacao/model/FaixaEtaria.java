package br.com.creditas.service.simulacao.model;

import java.math.BigDecimal;

public record FaixaEtaria(
        int idadeMin,
        int idadeMax,
        BigDecimal taxa
) {}