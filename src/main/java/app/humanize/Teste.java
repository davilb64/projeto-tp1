package app.humanize;

import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;

import java.util.Optional;
import java.util.Scanner;

public class Teste {

    public static void main(String[] args){
        Scanner entrada = new Scanner(System.in);
        System.out.println("Digite seu login: ");
        String login = entrada.nextLine();
        System.out.println("Digite sua senha: ");
        String senha = entrada.nextLine();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        Optional<Usuario> usuario = usuarioRepository.buscaUsuarioPorLogin(login);
        if (usuario.isPresent()) {
            if (senha.equals(usuario.get().getSenha())) {
                System.out.println("Entrou em: " + usuario.get().getPerfil());
            }
        }
    }
}
