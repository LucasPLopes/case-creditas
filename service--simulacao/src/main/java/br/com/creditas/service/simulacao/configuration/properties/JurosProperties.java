package br.com.creditas.service.simulacao.configuration.properties;

import br.com.creditas.service.simulacao.model.FaixaEtaria;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "juros")
public class JurosProperties {

    private List<FaixaEtaria> faixaEtaria;

    public List<FaixaEtaria> faixaEtaria() {
        return faixaEtaria;
    }

    public void setFaixaEtaria(List<FaixaEtaria> faixaEtaria) {
        this.faixaEtaria = faixaEtaria;
    }

    @Cacheable(value = "taxaPorIdade", key = "#idade")
    public BigDecimal getTaxaPorIdade(int idade) {
        return faixaEtaria.stream()
                .filter(f -> idade >= f.idadeMin() && idade <= f.idadeMax())
                .findFirst()
                .map(FaixaEtaria::taxa)
                .orElseThrow(() -> new IllegalArgumentException("Faixa etária não encontrada"));
    }
}
