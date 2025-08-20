package br.com.creditas.service.proposta.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "simulacao")
public class SimulacaoConfig {
    String baseUrl;
    String buscarSimulacao;
}
