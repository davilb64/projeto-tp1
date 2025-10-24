package app.humanize.util;

import app.humanize.model.Usuario;

/**
 * Classe Singleton para gerenciar a sessão do usuário logado.
 * Ela armazena o objeto Usuario e o disponibiliza para toda a aplicação.
 */
public final class UserSession {

    private static UserSession instance;
    private Usuario usuarioLogado;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(Usuario usuario) {
        this.usuarioLogado = usuario;
    }

    public void logout() {
        this.usuarioLogado = null;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
}