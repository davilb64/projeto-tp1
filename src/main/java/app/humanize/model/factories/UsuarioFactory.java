package app.humanize.model.factories;

import app.humanize.model.*;

public class UsuarioFactory {
    public Usuario createUsuario(String nome, String cpf, Endereco endereco, String email, String login, String senha, Perfil perfil) {
        if (perfil == Perfil.ADMINISTRADOR) {
            Administrador.AdministradorBuilder builder = new Administrador.AdministradorBuilder();
            builder.nome(nome);
            builder.cpf(cpf);
            builder.endereco(endereco);
            builder.email(email);
            builder.login(login);
            builder.senha(senha);
            builder.perfil(Perfil.ADMINISTRADOR);
            return builder.build();
        }
        else if (perfil == Perfil.GESTOR) {
            Gestor.GestorBuilder builder = new Gestor.GestorBuilder();
            builder.nome(nome);
            builder.cpf(cpf);
            builder.endereco(endereco);
            builder.email(email);
            builder.login(login);
            builder.senha(senha);
            builder.perfil(Perfil.GESTOR);
            return builder.build();
        }
        else if (perfil == Perfil.RECRUTADOR) {
            Recrutador.RecrutadorBuilder builder = new Recrutador.RecrutadorBuilder();
            builder.nome(nome);
            builder.cpf(cpf);
            builder.endereco(endereco);
            builder.email(email);
            builder.login(login);
            builder.senha(senha);
            builder.perfil(Perfil.RECRUTADOR);
            return builder.build();
        }
        else if (perfil == Perfil.FUNCIONARIO) {
            Funcionario.FuncionarioBuilder builder = new Funcionario.FuncionarioBuilder();
            builder.nome(nome);
            builder.cpf(cpf);
            builder.endereco(endereco);
            builder.email(email);
            builder.login(login);
            builder.senha(senha);
            builder.perfil(Perfil.FUNCIONARIO);
            return builder.build();
        }
        else return null;
    }
}
