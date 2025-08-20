package br.com.creditas.service.proposta.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Cliente(@NotNull(message = "Campo 'cliente.cpf' não pode ser nulo") @NotEmpty String cpf,
                      @NotNull(message = "Campo 'cliente.nome' não pode ser nulo") @NotEmpty String nome) {
}
