package br.com.creditas.service.simulacao.service.factory;

import org.springframework.stereotype.Component;

@Component
public class SimulacaoCalculatorFactory {

    private final TaxaFixaCalculator taxaFixaCalculator;
    private final TaxaVariavelCalculator taxaVariavelCalculator;

    public SimulacaoCalculatorFactory(TaxaFixaCalculator taxaFixaCalculator, TaxaVariavelCalculator taxaVariavelCalculator) {
        this.taxaFixaCalculator = taxaFixaCalculator;
        this.taxaVariavelCalculator = taxaVariavelCalculator;
    }

    public SimulacaoCalculator getCalculator(String modalidade) {
        if (modalidade == null || modalidade.equalsIgnoreCase("FIXA")) {
            return taxaFixaCalculator;
        }
        if (modalidade.equalsIgnoreCase("VARIAVEL")) {
            return taxaVariavelCalculator;
        }
        return taxaFixaCalculator; // default
    }
}
