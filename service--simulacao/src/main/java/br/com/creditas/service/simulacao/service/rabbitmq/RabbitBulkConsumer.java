package br.com.creditas.service.simulacao.service.rabbitmq;

import br.com.creditas.service.simulacao.dto.RabbitRequest;
import br.com.creditas.service.simulacao.service.BulkSimulacaoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitBulkConsumer {
    private final BulkSimulacaoService bulkService;

    public RabbitBulkConsumer(BulkSimulacaoService bulkService) {
        this.bulkService = bulkService;
    }

    @RabbitListener(queues = "${bulk.rabbitmq.simulacao.queue}", concurrency = "${bulk.rabbitmq.simulacao.concurrency}")
    public void processarMensagem(RabbitRequest request) {
        bulkService.processarItem(request.chaveComposta(), request.index(), request.request());
    }
}