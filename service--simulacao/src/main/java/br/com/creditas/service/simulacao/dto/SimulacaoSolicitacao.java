package br.com.creditas.service.simulacao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimulacaoSolicitacao(
        @NotNull(message = "O valor não pode ser nulo")
        @DecimalMin(value = "1.00", message = "O valor mínimo permitido é R$ 1,00")
        BigDecimal valor,

        @NotNull(message = "O prazo em meses é obrigatório")
        @Min(value = 1, message = "O prazo mínimo é de 1 meses")
        @Max(value = 120, message = "O prazo máximo é de 120 meses")
        Integer prazoMeses,

        @NotNull(message = "A data de nascimento é obrigatório, no formato yyyy-MM-dd")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dataNascimento,
        String modalidade,
        @NotNull(message = "Informar o campo 'registrar'")
        Boolean registrar
) { }

