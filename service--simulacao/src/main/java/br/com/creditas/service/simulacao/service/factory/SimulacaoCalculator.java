package br.com.creditas.service.simulacao.service.factory;

import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import br.com.creditas.service.simulacao.dto.SimulacaoResultado;

public sealed interface SimulacaoCalculator
        permits TaxaFixaCalculator, TaxaVariavelCalculator {

    SimulacaoResultado calcular(SimulacaoSolicitacao request);
}
