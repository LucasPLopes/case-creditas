package br.com.creditas.service.simulacao.service;

import br.com.creditas.service.simulacao.dto.SimulacaoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

import static br.com.creditas.service.simulacao.configuration.SimulacaoConstants.*;

@Slf4j
@Service
public class CalculatorService {

    public static BigDecimal calcularPMT(BigDecimal valor, BigDecimal taxa, int prazo) {
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

    public static BigDecimal getTaxaMensal(BigDecimal taxaAnual) {
        return taxaAnual.divide(BigDecimal.valueOf(12), SCALE_TAXA, ROUNDING);
    }

    public static int calcularIdade(SimulacaoRequest request) {
        return Period.between(request.dataNascimento(), LocalDate.now()).getYears();
    }

    public static BigDecimal calcularTotalJuros(SimulacaoRequest request, BigDecimal valorTotal) {
        return valorTotal.subtract(request.valor()).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal calcularValorTotal(SimulacaoRequest request, BigDecimal parcela) {
        return parcela.multiply(BigDecimal.valueOf(request.prazoMeses())).setScale(SCALE, ROUNDING);
    }
}
