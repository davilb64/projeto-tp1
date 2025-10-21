package app.humanize.service;

import app.humanize.exceptions.CpfInvalidoException;

public class ValidaCpf {
    public void validaCpf(String cpf) throws CpfInvalidoException {
        if (cpf.length() != 11) {
            throw new CpfInvalidoException("CPF invalido (comprimento)");
        }
        // Verifica se todos os dígitos são numéricos
        if (!cpf.matches("[0-9]{11}")) {
            throw new CpfInvalidoException("CPF invalido (formato)");
        }
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            throw new CpfInvalidoException("CPF invalido (dígitos iguais)");
        }

        if (!calculaCpf(cpf)) {
            throw new CpfInvalidoException("CPF invalido (digito verificador)");
        }
    }

    private boolean calculaCpf(String cpf) {
        int verificador1;
        int verificador2;

        //Cálculo do primeiro dígito
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int mult = 10 - i;
            int digito = Character.getNumericValue(cpf.charAt(i));
            sum = sum + (mult * digito);
        }

        int resto1 = sum % 11;
        verificador1 = (resto1 < 2) ? 0 : (11 - resto1);


        //Cálculo do segundo dígito
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            int mult = 11 - i;
            int digito = Character.getNumericValue(cpf.charAt(i));
            sum2 = sum2 + (mult * digito);
        }

        int resto2 = sum2 % 11;
        verificador2 = (resto2 < 2) ? 0 : (11 - resto2);

        int digitoReal1 = Character.getNumericValue(cpf.charAt(9));
        int digitoReal2 = Character.getNumericValue(cpf.charAt(10));

        return (verificador1 == digitoReal1) && (verificador2 == digitoReal2);
    }
}
