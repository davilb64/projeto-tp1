package app.humanize.service;

import app.humanize.exceptions.SenhaIncorretaException;
import app.humanize.exceptions.UsuarioNaoEncontradoException;
import app.humanize.exceptions.ValidacaoException;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class LoginService {
    private UsuarioRepository usuarioRepository;
    public LoginService() {
        usuarioRepository = new UsuarioRepository();
    }

    public Usuario autenticar(String login, String senha) throws ValidacaoException, SenhaIncorretaException, UsuarioNaoEncontradoException {
        if (login == null || login.isEmpty() || senha == null || senha.isEmpty()) {
            throw new ValidacaoException("Usuário / senha vazio");
        }

        Optional<Usuario> usuarioBusca = usuarioRepository.buscaUsuarioPorLogin(login);
        if (usuarioBusca.isEmpty()) {
            throw new UsuarioNaoEncontradoException("Seu usuário não foi encontrado");
        }

        Usuario usuario = usuarioBusca.get();

        if (!BCrypt.checkpw(senha, usuario.getSenha())) {
            throw new SenhaIncorretaException("Sua senha não corresponde ao usuário informado.");
        }

        return usuario;
    }
}
