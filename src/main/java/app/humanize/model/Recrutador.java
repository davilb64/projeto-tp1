package app.humanize.model;

public class Recrutador extends Usuario {
    //construtores
    public Recrutador(int id, String nome, String cpf, Endereco endereco, String email, String login, String senha) {
        super(nome, cpf, endereco, email, login, senha);
    }
    public Recrutador(){}

}
