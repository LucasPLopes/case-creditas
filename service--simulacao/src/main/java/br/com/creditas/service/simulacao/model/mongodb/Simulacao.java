package br.com.creditas.service.simulacao.model.mongodb;

import br.com.creditas.service.simulacao.dto.SimulacaoRequest;
import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("simulacao")
public record Simulacao(
        @Id String id,
        SimulacaoRequest request,
        SimulacaoResponse response

){}