package br.com.creditas.service.proposta.service;

import br.com.creditas.service.proposta.domain.mongodb.Proposta;
import br.com.creditas.service.proposta.dto.PropostaConsultaResponse;
import br.com.creditas.service.proposta.dto.PropostaRequest;
import br.com.creditas.service.proposta.dto.PropostaResponse;
import br.com.creditas.service.proposta.repository.PropostaRepository;
import br.com.creditas.service.proposta.validation.NegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PropostaService {
    private final SimulacaoClient simulacaoClient;
    private final PropostaRepository repository;

    public PropostaService(SimulacaoClient simulacaoClient, PropostaRepository repository) {
        this.simulacaoClient = simulacaoClient;
        this.repository = repository;
    }

    @Transactional
    public PropostaResponse incluirProposta(PropostaRequest request) {
        var simulacaoResponse = simulacaoClient.buscarSimulacao(request.idSimulacao());
        log.info("Incluindo proposta para cpf {}", request.cliente().cpf());
        var proposta = new Proposta(null, request.cliente(), simulacaoResponse, LocalDateTime.now());
        proposta = repository.save(proposta);
        log.info("Proposta {} incluida com sucesso", proposta.id());
        return new PropostaResponse(proposta.id());
    }

    public PropostaConsultaResponse buscarProposta(String id) {
        Optional<Proposta> opt = repository.findById(id);
        if (opt.isPresent()) {
            Proposta proposta = opt.get();
            return new PropostaConsultaResponse(proposta.id(), proposta.cliente(), proposta.simulacao(), proposta.simulacao().resultado().valorTotal());
        }
        throw new NegocioException("Proposta não encontrada", HttpStatus.NOT_FOUND);
    }

    public void deletarProposta(String id) {
        repository.deleteById(id);
    }
}
