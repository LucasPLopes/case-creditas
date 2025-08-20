package br.com.creditas.service.proposta.validation;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class NegocioException extends RuntimeException {
    private HttpStatus httpStatus;

    public NegocioException(String message) {
        super(message);
    }

    public NegocioException(String message, HttpStatus status) {
        super(message);
        httpStatus = status;
    }
}
