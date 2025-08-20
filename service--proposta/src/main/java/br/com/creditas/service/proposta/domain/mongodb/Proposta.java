package br.com.creditas.service.proposta.domain.mongodb;

import br.com.creditas.service.proposta.dto.Cliente;
import br.com.creditas.service.proposta.dto.SimulacaoResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("proposta")
public record Proposta(
    @Id
    String id,
    Cliente cliente,
    SimulacaoResponse simulacao,
    LocalDateTime dataCriacao) { }
