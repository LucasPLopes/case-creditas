package br.com.creditas.service.simulacao.repository;

import br.com.creditas.service.simulacao.model.mongodb.BulkProcess;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BulkProcessRepository extends MongoRepository<BulkProcess, String> {
    Optional<BulkProcess> findByCorrelationId(String correlationId);
}