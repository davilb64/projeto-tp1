package app.humanize.model;

import java.util.List;

public abstract class Usuario extends Pessoa {
    private String login;
    private String senha;
    private Perfil perfil;

    //construtores
    public Usuario(String nome, String cpf, Endereco endereco, String email, String login, String senha) {
        super(nome, cpf, endereco, email);
        this.login = login;
        this.senha = senha;
    }

    public Usuario() {}

    //metodos especiais
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfis) { this.perfil = perfil; }

    //metodos
    public boolean temPerfil(Perfil perfil) {
        return this.perfil != null;
    }

}
