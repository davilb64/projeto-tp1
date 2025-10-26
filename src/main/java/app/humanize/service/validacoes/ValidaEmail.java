package app.humanize.service.validacoes;

import app.humanize.exceptions.EmailInvalidoException;

import java.util.regex.Pattern;

public class ValidaEmail {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Valida se a string fornecida tem um formato de e-mail válido.
     *
     * @param email Email a ser validado.
     * @return true se o formato for válido.
     * @throws EmailInvalidoException Se o e-mail for nulo, vazio ou não corresponder ao formato esperado.
     */
    public boolean validaEmail(String email) throws EmailInvalidoException {
        if (email == null || email.trim().isEmpty()) {
            throw new EmailInvalidoException("O endereço de e-mail não pode ser vazio.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new EmailInvalidoException("O formato do e-mail '" + email + "' é inválido.");
        }


        return true;
    }
}
