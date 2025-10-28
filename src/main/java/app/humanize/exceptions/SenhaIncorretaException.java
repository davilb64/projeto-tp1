package app.humanize.exceptions;

public class SenhaIncorretaException extends LoginException {
    public SenhaIncorretaException(String message) { super(message); }
}
