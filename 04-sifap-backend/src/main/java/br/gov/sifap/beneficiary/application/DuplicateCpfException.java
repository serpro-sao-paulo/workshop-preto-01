package br.gov.sifap.beneficiary.application;

import br.gov.sifap.shared.CpfValidator;

public class DuplicateCpfException extends RuntimeException {
    public DuplicateCpfException(String cpf) {
        super("CPF já cadastrado: " + CpfValidator.mask(cpf));
    }
}
