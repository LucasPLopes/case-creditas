package br.com.creditas.service.simulacao.repository;

import br.com.creditas.service.simulacao.model.mongodb.Simulacao;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SimulacaoRepository extends MongoRepository<Simulacao, String> {
}
