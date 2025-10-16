package app.humanize.model;

public class Recrutador extends Usuario {

    // construtores
    private Recrutador(RecrutadorBuilder builder) {
        super(builder.nome, builder.cpf, builder.endereco, builder.email, builder.login, builder.senha, builder.perfil);
    }

    public Recrutador() {}

    //builder
    public static class RecrutadorBuilder {
        // Atributos de Usuario/Pessoa
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;

        public RecrutadorBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public RecrutadorBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public RecrutadorBuilder email(String email) {
            this.email = email;
            return this;
        }

        public RecrutadorBuilder endereco(Endereco endereco) {
            this.endereco = endereco;
            return this;
        }

        public RecrutadorBuilder login(String login) {
            this.login = login;
            return this;
        }

        public RecrutadorBuilder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public RecrutadorBuilder perfil(Perfil perfil) {
            this.perfil = perfil;
            return this;
        }

        public Recrutador build() {
            return new Recrutador(this);
        }
    }
}