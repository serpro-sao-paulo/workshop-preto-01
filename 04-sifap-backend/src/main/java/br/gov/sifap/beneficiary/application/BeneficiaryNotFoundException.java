package br.gov.sifap.beneficiary.application;

import br.gov.sifap.shared.CpfValidator;

import java.util.UUID;

public class BeneficiaryNotFoundException extends RuntimeException {
    public BeneficiaryNotFoundException(UUID id) {
        super("Beneficiário não encontrado: id=" + id);
    }
}
