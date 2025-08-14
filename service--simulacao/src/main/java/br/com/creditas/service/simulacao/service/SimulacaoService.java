package br.com.creditas.service.simulacao.service;

import br.com.creditas.service.simulacao.configuration.properties.JurosProperties;
import br.com.creditas.service.simulacao.dto.SimulacaoRequest;
import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SimulacaoService {
    private final JurosProperties jurosProperties;
    private static final int SCALE = 2;
    private static final int SCALE_TAXA = 5;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public SimulacaoService(JurosProperties jurosProperties) {
        this.jurosProperties = jurosProperties;
    }

    public SimulacaoResponse simular(SimulacaoRequest request) {
        log.debug("calculando simulacao para {}", request);
        int idade = calcularIdade(request);
        BigDecimal taxaAnual = getTaxaPorIdade(idade);
        BigDecimal taxaMensal = getTaxaMensal(taxaAnual);
        log.debug("taxa am {}, aa {}", taxaMensal, taxaAnual);

        BigDecimal parcela = calcularPMT(request.valor(), taxaMensal, request.prazoMeses())
                .setScale(SCALE, ROUNDING);
        BigDecimal valorTotal = parcela.multiply(BigDecimal.valueOf(request.prazoMeses()))
                .setScale(SCALE, ROUNDING);
        BigDecimal totalJuros = valorTotal.subtract(request.valor())
                .setScale(SCALE, ROUNDING);

        return new SimulacaoResponse(valorTotal, parcela, totalJuros, taxaAnual.setScale(SCALE_TAXA, ROUNDING));
    }

    private static BigDecimal getTaxaMensal(BigDecimal taxaAnual) {
        return taxaAnual.divide(BigDecimal.valueOf(12), SCALE_TAXA, ROUNDING);
    }

    private BigDecimal getTaxaPorIdade(int idade) {
        return jurosProperties.getTaxaPorIdade(idade);
    }

    private static int calcularIdade(SimulacaoRequest request) {
        return Period.between(request.dataNascimento(), LocalDate.now()).getYears();
    }

    private static BigDecimal calcularPMT(BigDecimal valor, BigDecimal taxa, int prazo) {
        MathContext precisao = new MathContext(10, RoundingMode.HALF_UP);
        // (1 + r)^(-n)
        BigDecimal base = BigDecimal.ONE.add(taxa);
        BigDecimal exponencial = base.pow(-prazo, precisao); // Potenciação com precisão
        // 1 - (1 + r)^(-n)
        BigDecimal denominador = BigDecimal.ONE.subtract(exponencial);
        // PV * r
        BigDecimal numerador = valor.multiply(taxa);
        // 4. Calcular PMT: (PV * r) / (1 - (1 + r)^(-n))
        BigDecimal pmt = numerador.divide(denominador, precisao);
        return pmt.setScale(2, RoundingMode.HALF_EVEN);

    }

    public List<SimulacaoResponse> simular(@Valid List<SimulacaoRequest> simulacoes) {
        return simulacoes.parallelStream().map(this::simular).toList();
    }
}
