package br.com.creditas.service.simulacao.resource.internal;

import br.com.creditas.service.simulacao.configuration.properties.BulkProperties;
import br.com.creditas.service.simulacao.dto.BulkSimulacaoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1/simulacoes")
public class InternalSimulacaoController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String kafkaRequestTopic;

    public InternalSimulacaoController(
            KafkaTemplate<String, Object> kafkaTemplate,
            BulkProperties bulkProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaRequestTopic = bulkProperties.getKafka().getRequest();
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> iniciarFluxoKafka(
            @RequestBody @Valid BulkSimulacaoRequest request) {

        kafkaTemplate.send(kafkaRequestTopic, request);
        return ResponseEntity.accepted().body("Solicitação de bulk enviada para Kafka");
    }
}