package br.com.creditas.service.simulacao.service;

import br.com.creditas.service.simulacao.configuration.properties.BulkProperties;
import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import br.com.creditas.service.simulacao.repository.BulkProcessRepository;
import br.com.creditas.service.simulacao.service.kafka.KafkaBulkProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.yaml")
class BulkSimulacaoServiceUnitTest {
    @Autowired
    private BulkProperties bulkProperties;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private KafkaBulkProducer kafkaProducer;

    @Mock
    private BulkProcessRepository bulkRepository;

    @Mock
    private SimulacaoService simulacaoService;

    @InjectMocks
    private BulkSimulacaoService service;

    @Test
    void deveEnviarRequestsEmLotesParaRabbit() {
        var requests = List.of(
                new SimulacaoSolicitacao(BigDecimal.valueOf(1000), 12, LocalDate.of(1950, 12, 31), "FIXA", false),
                new SimulacaoSolicitacao(BigDecimal.valueOf(1000), 12, LocalDate.of(1950, 12, 31), "FIXA", false),
                new SimulacaoSolicitacao(BigDecimal.valueOf(1000), 12, LocalDate.of(1950, 12, 31), "FIXA", false)
        );

        service.processarBulk("CANAL", "123", requests);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(any(String.class), any(List.class));
        verify(bulkRepository, atLeastOnce()).save(any());
    }
}
