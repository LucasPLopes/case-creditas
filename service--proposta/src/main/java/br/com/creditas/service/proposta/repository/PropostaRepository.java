package br.com.creditas.service.proposta.repository;

import br.com.creditas.service.proposta.domain.mongodb.Proposta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaRepository extends MongoRepository<Proposta, String> {
}
