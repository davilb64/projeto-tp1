package app.humanize.model;

public class Gestor extends Usuario {
    //construtores

    public Gestor(int id, String nome, String cpf, Endereco endereco, String email, String login, String senha) {
        super(nome, cpf, endereco, email, login, senha);
    }

    public Gestor() {}


}
