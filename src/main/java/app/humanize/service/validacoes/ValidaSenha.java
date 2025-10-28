package app.humanize.service.validacoes;

import app.humanize.exceptions.SenhaInvalidaException;

public class ValidaSenha {
    public void validaSenha(String senha) throws SenhaInvalidaException {
        if (senha == null || senha.isEmpty()) {
            throw new SenhaInvalidaException("Senha vazia");
        }

        // Regra 1: Comprimento Mínimo
        if (senha.length() < 8) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 8 caracteres");
        }

        // Regra 2: Deve conter pelo menos um número (.*[0-9].*)
        if (!senha.matches(".*[0-9].*")) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos um número");
        }

        // Regra 3: Deve conter pelo menos uma letra minúscula (.*[a-z].*)
        if (!senha.matches(".*[a-z].*")) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 letra minúscula");
        }

        // Regra 4: Deve conter pelo menos uma letra maiúscula (.*[A-Z].*)
        if (!senha.matches(".*[A-Z].*")) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 letra maiúscula");
        }

        // Regra 5: Deve conter um caractere especial
        if (!senha.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 caractere especial");
        }

    }
}
