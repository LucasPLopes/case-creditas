package br.com.creditas.service.proposta.service;

import br.com.creditas.service.proposta.configuration.SimulacaoConfig;
import br.com.creditas.service.proposta.dto.SimulacaoResponse;
import br.com.creditas.service.proposta.validation.ErrorInternoException;
import br.com.creditas.service.proposta.validation.NegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class SimulacaoClient {
    private final RestClient restClient;
    private final SimulacaoConfig simulacaoConfig;

    public SimulacaoClient( SimulacaoConfig simulacaoConfig) {
        this.simulacaoConfig = simulacaoConfig;
        this.restClient = RestClient.builder()
                .defaultStatusHandler(
                        HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw new NegocioException("Erro cliente: " + response.getStatusCode());
                        })
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new ErrorInternoException("Erro servidor: " + response.getStatusCode());
                        })
                .build();
    }

    public SimulacaoResponse buscarSimulacao(String id) {
        return restClient.get()
                .uri(this.simulacaoConfig.getBaseUrl() + this.simulacaoConfig.getBuscarSimulacao() , id)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, (request, response) -> {
                    throw new RuntimeException("Simulação não encontrada com ID: " + id);
                })
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("Erro ao consultar simulacao {}",   response.getBody());
                    throw new NegocioException("Erro do cliente com http status " + response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("Erro interno ao consultar simulacao {}",   response.getBody());
                    throw new ErrorInternoException("Erro interno ao consultar simulacao com http status " + response.getStatusCode());
                })
                .body(SimulacaoResponse.class);
    }

}
