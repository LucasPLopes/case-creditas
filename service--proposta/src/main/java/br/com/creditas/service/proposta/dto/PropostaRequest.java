package br.com.creditas.service.proposta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PropostaRequest(
        @NotNull(message = "Campo 'idSimulacao' não pode ser nulo")
        String idSimulacao,
        @Valid
        Cliente cliente
) {
}
