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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void processarBulk(String canal, String correlationId, List<SimulacaoSolicitacao> requests) {
        var chaveComposta = canal + "_" + correlationId;

        // Registrar início no MongoDB
        var bulkProcess = new BulkProcess(
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
        int batchSize = bulkProperties.getBatchSize();
        log.debug("Batch size {} para o chave {}", batchSize,chaveComposta );
        int totalRequests = requests.size();
        int batchCount = (int) Math.ceil((double) totalRequests / batchSize);

        for (int batchIndex = 0; batchIndex < batchCount; batchIndex++) {
            int fromIndex = batchIndex * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, totalRequests);

            List<SimulacaoSolicitacao> batch = requests.subList(fromIndex, toIndex);
            List<RabbitRequest> rabbitRequests = new ArrayList<>();

            for (int i = 0; i < batch.size(); i++) {
                int globalIndex = fromIndex + i;
                rabbitRequests.add(new RabbitRequest(chaveComposta, globalIndex, batch.get(i)));
            }

            rabbitTemplate.convertAndSend(
                    bulkProperties.getRabbitmq().getQueue(),
                    rabbitRequests
            );

            log.debug("Enviado lote {} de {} para RabbitMQ. Itens: {}-{}",
                    batchIndex + 1, batchCount, fromIndex, toIndex - 1);
        }
    }

    public void processarItem(String chaveComposta, int index, SimulacaoSolicitacao request) {
        try {
            var simulacaoResponse = simulacaoService.simular(request);
            armazenarResultado(chaveComposta, index, simulacaoResponse);
            verificarCompletude(chaveComposta);
        } catch (Exception e) {
            log.error("Erro no processamento de item: {}", chaveComposta, e);
            armazenarErro(chaveComposta, index, e.getMessage());
            verificarCompletude(chaveComposta);
        }
    }

    public void armazenarResultado(String chaveComposta, int index, SimulacaoResponse response) {
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
        var simulacoes = resultados.stream()
                .map(BulkItemResult::response)
                .filter(Objects::nonNull)
                .toList();

        String[] partes = chaveComposta.split("_", 2);
        String canal = partes[0];
        String correlationId = partes[1];

        kafkaProducer.enviarResposta(new BulkSimulacaoResponse(
                canal,
                correlationId,
                simulacoes
        ));
    }

    @Transactional
    private void atualizarStatusSucesso(String chaveComposta, List<BulkItemResult> resultados) {
        bulkProcessRepository.findById(chaveComposta).ifPresent(process -> {
            var responses = resultados.stream()
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


    @Transactional
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