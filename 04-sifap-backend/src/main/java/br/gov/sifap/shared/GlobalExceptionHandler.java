package br.gov.sifap.shared;

import br.gov.sifap.beneficiary.application.BeneficiaryNotFoundException;
import br.gov.sifap.beneficiary.application.DuplicateCpfException;
import br.gov.sifap.payment.domain.EligibilityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Mapeamento global de exceções para RFC 7807 ProblemDetail.
 * Garante que nunca expomos stack traces ou dados sensíveis.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("urn:sifap:error:validation"));
        return problem;
    }

    @ExceptionHandler(DuplicateCpfException.class)
    public ProblemDetail handleDuplicateCpf(DuplicateCpfException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("urn:sifap:error:duplicate-cpf"));
        return problem;
    }

    @ExceptionHandler(BeneficiaryNotFoundException.class)
    public ProblemDetail handleNotFound(BeneficiaryNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("urn:sifap:error:not-found"));
        return problem;
    }

    @ExceptionHandler(EligibilityException.class)
    public ProblemDetail handleEligibility(EligibilityException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("urn:sifap:error:not-eligible"));
        problem.setProperty("reason", ex.getReason().name());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Validation failed");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(URI.create("urn:sifap:error:validation"));
        return problem;
    }
}
