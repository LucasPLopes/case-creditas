package br.com.creditas.service.simulacao.model.mongodb;

import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import br.com.creditas.service.simulacao.dto.SimulacaoResultado;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("simulacao")
public record Simulacao(
        @Id String id,
        SimulacaoSolicitacao solicitacao,
        SimulacaoResultado resultado,
        LocalDateTime dataCriacao

){}