package br.com.creditas.service.proposta.dto;

import java.math.BigDecimal;

public record PropostaConsultaResponse(
        String numeroProposta,
        Cliente cliente,
        SimulacaoResponse simulacao,
        BigDecimal valorTotal
) {
}
