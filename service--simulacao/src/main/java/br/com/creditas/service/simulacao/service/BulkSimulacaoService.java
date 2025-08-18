package br.com.creditas.service.simulacao.service;


import br.com.creditas.service.simulacao.configuration.properties.BulkProperties;
import br.com.creditas.service.simulacao.dto.*;
import br.com.creditas.service.simulacao.model.mongodb.BulkProcess;
import br.com.creditas.service.simulacao.repository.BulkProcessRepository;
import br.com.creditas.service.simulacao.service.kafka.KafkaBulkProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BulkSimulacaoService {
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaBulkProducer kafkaProducer;
    private final BulkProcessRepository bulkProcessRepository;
    private final BulkProperties bulkProperties;
    private final SimulacaoService simulacaoService;


    public BulkSimulacaoService(RabbitTemplate rabbitTemplate,
                                RedisTemplate<String, Object> redisTemplate,
                                KafkaBulkProducer kafkaProducer,
                                BulkProcessRepository bulkProcessRepository,
                                BulkProperties bulkProperties,
                                SimulacaoService simulacaoService) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        this.kafkaProducer = kafkaProducer;
        this.bulkProcessRepository = bulkProcessRepository;
        this.bulkProperties = bulkProperties;
        this.simulacaoService = simulacaoService;
    }

    public void processarBulk(String canal, String correlationId, List<SimulacaoSolicitacao> requests) {
        String chaveComposta = canal + "_" + correlationId;

        // Registrar início no MongoDB
        BulkProcess bulkProcess = new BulkProcess(
                chaveComposta,
                canal,
                correlationId,
                BulkProcess.StatusProcessamento.RECEBIDO,
                Instant.now(),
                null,
                new BulkSimulacaoRequest(canal, correlationId, requests),
                null
        );
        bulkProcessRepository.save(bulkProcess);

        // Atualizar para status PROCESSANDO
        bulkProcess = bulkProcess.withStatus(BulkProcess.StatusProcessamento.PROCESSANDO);
        bulkProcessRepository.save(bulkProcess);

        try {
            // Armazenar metadados no Redis com TTL
            redisTemplate.opsForValue().set(
                    "bulk:total:" + chaveComposta,
                    requests.size(),
                    bulkProperties.getRedis().getTtl(),
                    TimeUnit.SECONDS
            );

            redisTemplate.opsForValue().set(
                    "bulk:processed:" + chaveComposta,
                    0,
                    bulkProperties.getRedis().getTtl(),
                    TimeUnit.SECONDS
            );

            // Criar chave de resultados com TTL
            redisTemplate.expire(
                    "bulk:results:" + chaveComposta,
                    bulkProperties.getRedis().getTtl(),
                    TimeUnit.SECONDS
            );

            // Enviar todos os itens para RabbitMQ
            enviarParaRabbit(chaveComposta, requests);
        } catch (Exception e) {
            log.error("Erro no processamento bulk: {}", chaveComposta, e);
            atualizarStatusFalha(chaveComposta, e.getMessage());
        }
    }

    private void enviarParaRabbit(String chaveComposta, List<SimulacaoSolicitacao> requests) {
        for (int i = 0; i < requests.size(); i++) {
            RabbitRequest request = new RabbitRequest(chaveComposta, i, requests.get(i));
            rabbitTemplate.convertAndSend(
                    bulkProperties.getRabbitmq().getQueue(),
                    request
            );
        }
    }

    public void processarItem(String chaveComposta, int index, SimulacaoSolicitacao request) {
        try {
            SimulacaoResponse simulacaoResponse = simulacaoService.simular(request);
            armazenarResultado(chaveComposta, index, simulacaoResponse.resultado());
            verificarCompletude(chaveComposta);
        } catch (Exception e) {
            log.error("Erro no processamento de item: {}", chaveComposta, e);
            armazenarErro(chaveComposta, index, e.getMessage());
            verificarCompletude(chaveComposta);
        }
    }

    public void armazenarResultado(String chaveComposta, int index, SimulacaoResultado response) {
        // Armazenar resultado individual
        redisTemplate.opsForHash().put(
                "bulk:results:" + chaveComposta,
                String.valueOf(index),
                new BulkItemResult(response, null)
        );

        // Atualizar contador
        redisTemplate.opsForValue().increment("bulk:processed:" + chaveComposta, 1);
    }

    public void armazenarErro(String chaveComposta, int index, String erro) {
        // Armazenar erro individual
        redisTemplate.opsForHash().put(
                "bulk:results:" + chaveComposta,
                String.valueOf(index),
                new BulkItemResult(null, erro)
        );

        // Atualizar contador
        redisTemplate.opsForValue().increment("bulk:processed:" + chaveComposta, 1);
    }

    public void verificarCompletude(String chaveComposta) {
        var total = (Integer) redisTemplate.opsForValue().get("bulk:total:" + chaveComposta);
        var processados = (Integer)  redisTemplate.opsForValue().get("bulk:processed:" + chaveComposta);

        if (processados != null && processados.equals(total)) {
            List<BulkItemResult> resultados = coletarResultados(chaveComposta, total);
            enviarRespostaKafka(chaveComposta, resultados);
            atualizarStatusSucesso(chaveComposta, resultados);
            limparDadosRedis(chaveComposta);
        }
    }

    private List<BulkItemResult> coletarResultados(String chaveComposta, int total) {
        List<BulkItemResult> resultados = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            BulkItemResult result = (BulkItemResult) redisTemplate.opsForHash()
                    .get("bulk:results:" + chaveComposta, String.valueOf(i));
            resultados.add(result);
        }
        return resultados;
    }

    private void enviarRespostaKafka(String chaveComposta, List<BulkItemResult> resultados) {
        List<SimulacaoResultado> responses = resultados.stream()
                .filter(r -> r.response() != null)
                .map(BulkItemResult::response)
                .toList();

        String[] partes = chaveComposta.split("_", 2);
        String canal = partes[0];
        String correlationId = partes[1];

        kafkaProducer.enviarResposta(new BulkSimulacaoResponse(
                canal,
                correlationId,
                responses
        ));
    }

    private void atualizarStatusSucesso(String chaveComposta, List<BulkItemResult> resultados) {
        bulkProcessRepository.findById(chaveComposta).ifPresent(process -> {
            List<SimulacaoResultado> responses = resultados.stream()
                    .map(BulkItemResult::response)
                    .filter(Objects::nonNull)
                    .toList();

            BulkProcess atualizado = new BulkProcess(
                    process.id(),
                    process.canal(),
                    process.correlationId(),
                    BulkProcess.StatusProcessamento.SUCESSO,
                    process.inicio(),
                    Instant.now(),
                    process.request(),
                    new BulkSimulacaoResponse(
                            process.canal(),
                            process.correlationId(),
                            responses
                    )
            );
            bulkProcessRepository.save(atualizado);
        });
    }

    private void atualizarStatusFalha(String chaveComposta, String erro) {
        bulkProcessRepository.findById(chaveComposta).ifPresent(process -> {
            BulkProcess atualizado = new BulkProcess(
                    process.id(),
                    process.canal(),
                    process.correlationId(),
                    BulkProcess.StatusProcessamento.FALHA,
                    process.inicio(),
                    Instant.now(),
                    process.request(),
                    new BulkSimulacaoResponse(
                            process.canal(),
                            process.correlationId(),
                            null
                    )
            );
            bulkProcessRepository.save(atualizado);
        });
    }

    private void limparDadosRedis(String chaveComposta) {
        redisTemplate.delete("bulk:total:" + chaveComposta);
        redisTemplate.delete("bulk:processed:" + chaveComposta);
        redisTemplate.delete("bulk:results:" + chaveComposta);
    }

}