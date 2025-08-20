package br.com.creditas.service.simulacao.service.factory;

import br.com.creditas.service.simulacao.dto.SimulacaoResultado;
import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public final class TaxaVariavelCalculator implements SimulacaoCalculator {

    @Override
    public SimulacaoResultado calcular(SimulacaoSolicitacao request) {
        throw new UnsupportedOperationException("Operacao de taxa variavel nao suportada no momento");
    }
}
