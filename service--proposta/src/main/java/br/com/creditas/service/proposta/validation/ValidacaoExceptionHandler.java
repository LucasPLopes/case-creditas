package br.com.creditas.service.proposta.validation;


import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidacaoExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Erro de validação");
        problemDetail.setDetail("Um ou mais campos estão inválidos.");
        problemDetail.setProperty("timestamp", getLocalTime());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        problemDetail.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleJsonParseError(HttpMessageNotReadableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Erro de leitura do corpo da requisição");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", getLocalTime());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ProblemDetail> handleJsonParseError(UnsupportedOperationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Operacao não permitida");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", getLocalTime());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ProblemDetail> handleException(NegocioException ex) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if (ex.getHttpStatus() != null) {
            httpStatus = ex.getHttpStatus();
        }

        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setTitle("Erro de negócio");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", getLocalTime());

        return ResponseEntity.status(httpStatus).body(problemDetail);
    }

    @ExceptionHandler(ErrorInternoException.class)
    public ResponseEntity<ProblemDetail> handleException(ErrorInternoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Erro interno");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", getLocalTime());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    private static String getLocalTime() {
        return LocalDateTime.now().toString();
    }
}
