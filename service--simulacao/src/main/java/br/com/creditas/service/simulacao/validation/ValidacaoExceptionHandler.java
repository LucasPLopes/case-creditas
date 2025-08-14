package br.com.creditas.service.simulacao.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidacaoExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Erro de validação");
        problemDetail.setDetail("Um ou mais campos estão inválidos.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

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
        problemDetail.setDetail("O formato da data está inválido. Use o padrão yyyy-MM-dd.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(problemDetail);
    }
}
