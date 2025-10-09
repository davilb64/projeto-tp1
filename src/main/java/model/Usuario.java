package model;

abstract class Usuario extends Pessoa {
    private String login;
    private String senha;

    //construtor
    public Usuario(int id, String nome, String cpf, Endereco endereco, String email, String login, String senha) {
        super(id, nome, cpf, endereco, email);
        this.login = login;
        this.senha = senha;
    }

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

    @Override
    public String toString() {
        return "Usuario{" +
                "login='" + login + '\'' +
                ", senha='" + senha + '\'' +
                '}';
    }

    //metodos
    public boolean autenticar(String senha){
        return this.senha.equals(senha);
    }
}
