package br.com.creditas.service.simulacao.service.rabbitmq;

import br.com.creditas.service.simulacao.dto.RabbitRequest;
import br.com.creditas.service.simulacao.service.BulkSimulacaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RabbitBulkConsumer {
    private final BulkSimulacaoService bulkService;

    public RabbitBulkConsumer(BulkSimulacaoService bulkService) {
        this.bulkService = bulkService;
    }

    @RabbitListener(queues = "${bulk.rabbitmq.simulacao.queue}", concurrency = "${bulk.rabbitmq.simulacao.concurrency}")
    public void processarMensagem(List<RabbitRequest> requests) {
        log.info("Recebido lote com {} itens", requests.size());
        for (RabbitRequest request : requests) {
            bulkService.processarItem(
                    request.chaveComposta(),
                    request.index(),
                    request.request()
            );
        }
    }
}