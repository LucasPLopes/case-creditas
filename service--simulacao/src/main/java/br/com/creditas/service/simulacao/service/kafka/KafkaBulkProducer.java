package br.com.creditas.service.simulacao.service.kafka;

import br.com.creditas.service.simulacao.configuration.properties.BulkProperties;
import br.com.creditas.service.simulacao.dto.BulkSimulacaoResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaBulkProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BulkProperties bulkProperties;

    public KafkaBulkProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            BulkProperties bulkProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.bulkProperties = bulkProperties;
    }

    public void enviarResposta(BulkSimulacaoResponse response) {
        kafkaTemplate.send(
                bulkProperties.getKafka().getResponse(),
                response
        );
    }
}