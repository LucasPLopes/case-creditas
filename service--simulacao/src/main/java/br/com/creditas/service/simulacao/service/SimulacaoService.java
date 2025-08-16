package br.com.creditas.service.simulacao.service;

import br.com.creditas.service.simulacao.configuration.properties.JurosProperties;
import br.com.creditas.service.simulacao.dto.SimulacaoRequest;
import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.ROUNDING;
import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.SCALE_TAXA;

@Slf4j
@Service
public class SimulacaoService {
    private final JurosProperties jurosProperties;
    public SimulacaoService(JurosProperties jurosProperties) {
        this.jurosProperties = jurosProperties;
    }

    public SimulacaoResponse simular(SimulacaoRequest request) {
        log.info("Simulacao iniciada");
        return calcularSimulacao(request);
    }

    public List<SimulacaoResponse> simular(List<SimulacaoRequest> simulacoes) {
        return simulacoes.parallelStream().map(this::calcularSimulacao).toList();
    }

    private SimulacaoResponse calcularSimulacao(SimulacaoRequest request) {
        BigDecimal taxaAnual = getTaxaPorIdade(CalculatorService.calcularIdade(request));
        BigDecimal taxaMensal = CalculatorService.getTaxaMensal(taxaAnual);
        log.debug("taxa am {}, aa {}", taxaMensal, taxaAnual);
        BigDecimal parcela = CalculatorService.calcularPMT(request.valor(), taxaMensal, request.prazoMeses());
        BigDecimal valorTotal = CalculatorService.calcularValorTotal(request, parcela);
        BigDecimal totalJuros = CalculatorService.calcularTotalJuros(request, valorTotal);

        return new SimulacaoResponse(valorTotal, parcela, totalJuros, taxaAnual.setScale(SCALE_TAXA, ROUNDING));
    }

    private BigDecimal getTaxaPorIdade(int idade) {
        return jurosProperties.getTaxaPorIdade(idade);
    }

}
