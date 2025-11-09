package app.humanize.model;

public abstract class Usuario extends Pessoa {
    private String login;
    private String senha;
    private Perfil perfil;
    private String idiomaPreferencial;

    // Construtores
    public Usuario(String nome, String cpf, Endereco endereco, String email, String login, String senha, Perfil perfil, String idiomaPreferencial) {
        super(nome, cpf, endereco, email);
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
        this.idiomaPreferencial = (idiomaPreferencial == null || idiomaPreferencial.isEmpty()) ? "pt_BR" : idiomaPreferencial;
    }

    protected Usuario(String nome, String cpf, Endereco endereco, String email) {
        super(nome, cpf, endereco, email);
        this.idiomaPreferencial = "pt_BR";
    }

    public Usuario() {
        this.idiomaPreferencial = "pt_BR";
    }


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

    public String getIdiomaPreferencial() {
        return idiomaPreferencial;
    }

    public void setIdiomaPreferencial(String idiomaPreferencial) {
        this.idiomaPreferencial = idiomaPreferencial;
    }


    public boolean temPerfil() {
        return this.perfil != null;
    }
}