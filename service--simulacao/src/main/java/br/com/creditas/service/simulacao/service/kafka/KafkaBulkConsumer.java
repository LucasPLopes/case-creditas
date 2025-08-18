package br.com.creditas.service.simulacao.service.kafka;

import br.com.creditas.service.simulacao.dto.BulkSimulacaoRequest;
import br.com.creditas.service.simulacao.service.BulkSimulacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaBulkConsumer {
    private final BulkSimulacaoService bulkService;

    @KafkaListener(
            topics = "${bulk.kafka.request}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirMensagem(BulkSimulacaoRequest request) {
        log.info("Recebida solicitação bulk: {} - {}", request.canalOrigem(), request.correlationId());
        bulkService.processarBulk(
                request.canalOrigem(),
                request.correlationId(),
                request.simulacoes()
        );
    }
}