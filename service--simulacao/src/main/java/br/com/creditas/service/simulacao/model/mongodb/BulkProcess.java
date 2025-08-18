package br.com.creditas.service.simulacao.model.mongodb;


import br.com.creditas.service.simulacao.dto.BulkSimulacaoRequest;
import br.com.creditas.service.simulacao.dto.BulkSimulacaoResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("bulk_process")
public record BulkProcess(
        @Id String id,
        String correlationId,
        String canal,
        StatusProcessamento status,
        Instant inicio,
        Instant fim,
        BulkSimulacaoRequest request,
        BulkSimulacaoResponse response
) {
    public BulkProcess withStatus(StatusProcessamento newStatus) {
        return new BulkProcess(
                id, canal, correlationId, newStatus, inicio, fim, request, response
        );
    }
    public enum StatusProcessamento {
        RECEBIDO, PROCESSANDO, SUCESSO, FALHA
    }
}