package org.allservice.exceptions;

import org.allservice.exceptions.records.StandardError;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandardError err = new StandardError(
                status.value(),
                "Recurso não encontrado",
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(err);
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> businessException(BusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError err = new StandardError(
                status.value(),
                "Regra de negócio violada",
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataIntegrityException.class)
    public ResponseEntity<StandardError> dataIntegrity(DataIntegrityException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandardError err = new StandardError(
                status.value(),
                "Violação de integridade de dados",
                e.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> dataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError err = new StandardError(
                status.value(),
                "Erro de integridade no banco de dados",
                "Verifique se todos os campos obrigatórios foram preenchidos corretamente ou se há dados duplicados.",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler({Exception.class, org.springframework.dao.DataIntegrityViolationException.class})
    public ResponseEntity<StandardError> handleAllExceptions(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Se for um erro de violação de integridade do banco (ex: stock null), podemos retornar 400 Bad Request
        if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
            status = HttpStatus.BAD_REQUEST;
        }

        StandardError err = new StandardError(
                status.value(),
                status == HttpStatus.BAD_REQUEST ? "Erro de integridade no banco de dados" : "Erro interno no servidor",
                "Ocorreu um erro ao processar a requisição. Verifique os dados enviados.",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(err);
    }
}
