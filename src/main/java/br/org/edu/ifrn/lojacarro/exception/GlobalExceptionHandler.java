package br.org.edu.ifrn.lojacarro.exception;

import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CHAVE_ERRO = "error";

    @ExceptionHandler(InputValidator.ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(InputValidator.ValidationException ex) {
        log.warn("Validacao falhou: {}", ex.getMessage());
        return resposta(ex.getMessage(), HttpStatus.valueOf(ex.getStatusCode()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        if (status.is5xxServerError()) {
            log.error("Erro {}: {}", status.value(), ex.getReason(), ex);
        } else {
            log.warn("Requisicao recusada com {}: {}", status.value(), ex.getReason());
        }
        return resposta(ex.getReason(), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());
        return resposta(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException() {
        log.warn("Corpo da requisicao veio ilegivel ou com JSON quebrado");
        return resposta("Invalid request body", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parametro '{}' com valor em formato errado", ex.getName());
        return resposta("Valor inválido para " + ex.getName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Rota inexistente: /{}", ex.getResourcePath());
        return resposta("Not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Metodo {} nao suportado nessa rota", ex.getMethod());
        return resposta("Method not allowed", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return resposta("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> resposta(String mensagem, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put(CHAVE_ERRO, mensagem);
        return new ResponseEntity<>(response, status);
    }
}
