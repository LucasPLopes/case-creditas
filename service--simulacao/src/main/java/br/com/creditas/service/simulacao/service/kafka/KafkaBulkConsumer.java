package br.com.creditas.service.simulacao.service.kafka;

import br.com.creditas.service.simulacao.dto.BulkSimulacaoRequest;
import br.com.creditas.service.simulacao.dto.BulkSimulacaoResponse;
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
        log.info("Recebida solicitação bulk canal{} - correlation {} e tamanho {}",
                request.canalOrigem(),
                request.correlationId(),
                request.simulacoes().size());
        bulkService.processarBulk(
                request.canalOrigem(),
                request.correlationId(),
                request.simulacoes()
        );
    }

    @KafkaListener(
            topics = "${bulk.kafka.response}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirMensagem(BulkSimulacaoResponse response){
            log.info("Recebido processamento para o canal {} - correlation {} com total de simulacoes {}",
                    response.canal(),
                    response.correlationId(),
                    response.responses().size());
            response.responses().forEach(System.out::println);
    }

}