package br.com.creditas.service.proposta.validation;

public class ErrorInternoException extends RuntimeException {
    public ErrorInternoException(String message) {
        super(message);
    }
}
