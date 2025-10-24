package app.humanize.model;

public class Administrador extends Usuario {

    //builder
    public static class AdministradorBuilder {
        // Atributos de Usuario/Pessoa
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;

        public AdministradorBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public AdministradorBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }


        public AdministradorBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AdministradorBuilder endereco(Endereco endereco) {
            this.endereco = endereco;
            return this;
        }

        public AdministradorBuilder login(String login) {
            this.login = login;
            return this;
        }

        public AdministradorBuilder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public AdministradorBuilder perfil(Perfil perfil) {
            this.perfil = perfil;
            return this;
        }

        public Administrador build() {
            return new Administrador(this);
        }
    }

    //construtores

    private Administrador(AdministradorBuilder builder) {
        super(builder.nome, builder.cpf, builder.endereco, builder.email, builder.login, builder.senha, builder.perfil);
    }

    public Administrador() {}
}