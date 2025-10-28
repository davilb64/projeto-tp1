package app.humanize.model;

public abstract class Usuario extends Pessoa {
    private String login;
    private String senha;
    private Perfil perfil;

    // Construtores
    public Usuario(String nome, String cpf, Endereco endereco, String email, String login, String senha, Perfil perfil) {
        super(nome, cpf, endereco, email);
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
    }

    protected Usuario(String nome, String cpf, Endereco endereco, String email) {
        super(nome, cpf, endereco, email);
    }

    public Usuario() {}


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

    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    // metodos
    public boolean temPerfil() {
        return this.perfil != null;
    }
}