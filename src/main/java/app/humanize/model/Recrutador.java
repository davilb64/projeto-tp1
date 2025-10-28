package app.humanize.model;

import java.time.LocalDate;

public class Recrutador extends Funcionario {

    private Recrutador(RecrutadorBuilder builder) {
        super(
                builder.nome, builder.cpf, builder.endereco, builder.email,
                builder.login, builder.senha, builder.perfil,
                builder.matricula, builder.periodo,
                builder.receita, builder.despesas, builder.salario,
                builder.cargo, builder.regime, builder.departamento,
                builder.caminhoFoto // NOVO PARÂMETRO
        );
    }

    public Recrutador() {}

    public static class RecrutadorBuilder {
        private int matricula;
        private int periodo;
        private double receita;
        private double despesas;
        private double salario;
        private String cargo;
        private Regime regime;
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;
        private String departamento;
        private String caminhoFoto;

        public RecrutadorBuilder caminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; return this; }
        public RecrutadorBuilder regime(Regime regime) { this.regime = regime; return this; }
        public RecrutadorBuilder cargo(String cargo) { this.cargo = cargo; return this; }
        public RecrutadorBuilder matricula(int matricula) { this.matricula = matricula; return this; }
        public RecrutadorBuilder periodo(int periodo) { this.periodo = periodo; return this; }
        public RecrutadorBuilder receita(double receita) { this.receita = receita; return this; }
        public RecrutadorBuilder despesas(double despesas) { this.despesas = despesas; return this; }
        public RecrutadorBuilder salario(double salario) { this.salario = salario; return this; }
        public RecrutadorBuilder nome(String nome) { this.nome = nome; return this; }
        public RecrutadorBuilder cpf(String cpf) { this.cpf = cpf; return this; }
        public RecrutadorBuilder email(String email) { this.email = email; return this; }
        public RecrutadorBuilder endereco(Endereco endereco) { this.endereco = endereco; return this; }
        public RecrutadorBuilder login(String login) { this.login = login; return this; }
        public RecrutadorBuilder senha(String senha) { this.senha = senha; return this; }
        public RecrutadorBuilder perfil(Perfil perfil) { this.perfil = perfil; return this; }
        public RecrutadorBuilder departamento(String departamento) { this.departamento = departamento; return this; }


        public Recrutador build() {
            return new Recrutador(this);
        }
    }
}