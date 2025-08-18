package br.com.creditas.service.simulacao.configuration.properties;

import br.com.creditas.service.simulacao.model.FaixaEtaria;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "juros")
public class JurosProperties {
    private List<FaixaEtaria> faixaEtaria;

    @Cacheable(value = "taxaPorIdade", key = "#idade")
    public BigDecimal getTaxaPorIdade(int idade) {
        return faixaEtaria.stream()
                .filter(f -> idade >= f.idadeMin() && idade <= f.idadeMax())
                .findFirst()
                .map(FaixaEtaria::taxa)
                .orElseThrow(() -> new IllegalArgumentException("Faixa etária não encontrada"));
    }
}
