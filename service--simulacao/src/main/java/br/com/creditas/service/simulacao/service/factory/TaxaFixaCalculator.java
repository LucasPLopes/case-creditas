package br.com.creditas.service.simulacao.service.factory;

import br.com.creditas.service.simulacao.configuration.properties.JurosProperties;
import br.com.creditas.service.simulacao.dto.SimulacaoResultado;
import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.ROUNDING;
import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.SCALE_TAXA;

@Component
public final class TaxaFixaCalculator implements SimulacaoCalculator {

    private final JurosProperties jurosProperties;

    public TaxaFixaCalculator(JurosProperties jurosProperties) {
        this.jurosProperties = jurosProperties;
    }

    @Override
    public SimulacaoResultado calcular(SimulacaoSolicitacao request) {
        BigDecimal taxaAnual = jurosProperties.getTaxaPorIdade(
                CalculatorService.calcularIdade(request));
        BigDecimal taxaMensal = CalculatorService.getTaxaMensal(taxaAnual);
        BigDecimal parcela = CalculatorService.calcularPMT(request.valor(), taxaMensal, request.prazoMeses());
        BigDecimal valorTotal = CalculatorService.calcularValorTotal(request, parcela);
        BigDecimal totalJuros = CalculatorService.calcularTotalJuros(request, valorTotal);

        return new SimulacaoResultado(valorTotal, parcela, totalJuros,
                taxaAnual.setScale(SCALE_TAXA, ROUNDING));
    }
}
