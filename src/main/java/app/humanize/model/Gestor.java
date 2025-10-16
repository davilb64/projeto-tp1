package app.humanize.model;

public class Gestor extends Usuario {

    //builder
    public static class GestorBuilder {
        // Atributos de Usuario/Pessoa
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;

        public GestorBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public GestorBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public GestorBuilder email(String email) {
            this.email = email;
            return this;
        }

        public GestorBuilder endereco(Endereco endereco) {
            this.endereco = endereco;
            return this;
        }

        public GestorBuilder login(String login) {
            this.login = login;
            return this;
        }

        public GestorBuilder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public GestorBuilder perfil(Perfil perfil) {
            this.perfil = perfil;
            return this;
        }

        // Método 'build': Retorna a instância final
        public Gestor build() {
            return new Gestor(this);
        }
    }

    private Gestor(GestorBuilder builder) {
        super(builder.nome, builder.cpf, builder.endereco, builder.email, builder.login, builder.senha, builder.perfil);
    }

    public Gestor() {}

}