package br.com.creditas.service.simulacao.service;

import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import br.com.creditas.service.simulacao.dto.SimulacaoResultado;
import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import br.com.creditas.service.simulacao.model.mongodb.Simulacao;
import br.com.creditas.service.simulacao.repository.SimulacaoRepository;
import br.com.creditas.service.simulacao.service.factory.SimulacaoCalculatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SimulacaoService {
    private final SimulacaoRepository simulacaoRepository;
    private final SimulacaoCalculatorFactory calculatorFactory;

    public SimulacaoService(SimulacaoRepository simulacaoRepository, SimulacaoCalculatorFactory calculatorFactory) {
        this.simulacaoRepository = simulacaoRepository;
        this.calculatorFactory = calculatorFactory;
    }

    @Transactional
    public SimulacaoResponse simular(SimulacaoSolicitacao request) {
        log.info("Simulacao iniciada");
        SimulacaoResultado resultado = calculatorFactory
                .getCalculator(request.modalidade())
                .calcular(request);

        if (request.registrar()) {
            Simulacao simulacao = new Simulacao(null, request, resultado, LocalDateTime.now());
            simulacao = simulacaoRepository.save(simulacao);
            return new SimulacaoResponse(simulacao.id(), simulacao.resultado());
        }

        return new SimulacaoResponse(null, resultado);
    }

    public List<SimulacaoResultado> simular(List<SimulacaoSolicitacao> simulacoes) {
        return simulacoes.parallelStream()
                .map(req -> calculatorFactory.getCalculator(req.modalidade()).calcular(req))
                .toList();
    }
    public SimulacaoResponse buscarSimulacao(String id) {
        var simulacao = simulacaoRepository.findById(id);
        return simulacao.map(value -> new SimulacaoResponse(value.id(), value.resultado())).orElse(null);
    }
}