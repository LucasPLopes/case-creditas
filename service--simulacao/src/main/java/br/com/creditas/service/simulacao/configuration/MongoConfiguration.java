package br.com.creditas.service.simulacao.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "br.com.creditas.service.simulacao.repository")
@EntityScan(basePackages = "br.com.creditas.service.simulacao.model.mongodb")
public class MongoConfiguration {
}
