package br.com.creditas.service.simulacao.service;

import br.com.creditas.service.simulacao.configuration.properties.JurosProperties;
import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import br.com.creditas.service.simulacao.dto.SimulacaoResultado;
import br.com.creditas.service.simulacao.model.mongodb.Simulacao;
import br.com.creditas.service.simulacao.repository.SimulacaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.ROUNDING;
import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.SCALE_TAXA;

@Slf4j
@Service
public class SimulacaoService {
    private final JurosProperties jurosProperties;
    private final SimulacaoRepository simulacaoRepository;

    public SimulacaoService(JurosProperties jurosProperties, SimulacaoRepository simulacaoRepository) {
        this.jurosProperties = jurosProperties;
        this.simulacaoRepository = simulacaoRepository;
    }

    @Transactional
    public SimulacaoResponse simular(SimulacaoSolicitacao request) {
        log.info("Simulacao iniciada");
        SimulacaoResultado resultado = calcularSimulacao(request);
        if (request.registrar()) {
            Simulacao simulacao = new Simulacao(null, request, resultado, LocalDateTime.now());
            simulacao = simulacaoRepository.save(simulacao);
            return new SimulacaoResponse(simulacao.id(), simulacao.resultado());
        }

        return new SimulacaoResponse(null, resultado);
    }

    public List<SimulacaoResultado> simular(List<SimulacaoSolicitacao> simulacoes) {
        return simulacoes.parallelStream().map(this::calcularSimulacao).toList();
    }

    public SimulacaoResponse buscarSimulacao(String id) {
        var simulacao = simulacaoRepository.findById(id);
        return simulacao.map(value -> new SimulacaoResponse(value.id(), value.resultado())).orElse(null);
    }

    private SimulacaoResultado calcularSimulacao(SimulacaoSolicitacao request) {
        BigDecimal taxaAnual = getTaxaPorIdade(CalculatorService.calcularIdade(request));
        BigDecimal taxaMensal = CalculatorService.getTaxaMensal(taxaAnual);
        log.debug("taxa am {}, aa {}", taxaMensal, taxaAnual);
        BigDecimal parcela = CalculatorService.calcularPMT(request.valor(), taxaMensal, request.prazoMeses());
        BigDecimal valorTotal = CalculatorService.calcularValorTotal(request, parcela);
        BigDecimal totalJuros = CalculatorService.calcularTotalJuros(request, valorTotal);

        return new SimulacaoResultado(valorTotal, parcela, totalJuros, taxaAnual.setScale(SCALE_TAXA, ROUNDING));
    }

    private BigDecimal getTaxaPorIdade(int idade) {
        return jurosProperties.getTaxaPorIdade(idade);
    }

}
