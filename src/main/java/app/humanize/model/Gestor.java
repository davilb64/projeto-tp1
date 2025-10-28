package app.humanize.model;

import java.time.LocalDate;

public class Gestor extends Funcionario {

    private Gestor(GestorBuilder builder) {
        super(
                builder.nome, builder.cpf, builder.endereco, builder.email,
                builder.login, builder.senha, builder.perfil,
                builder.matricula, builder.periodo,
                builder.receita, builder.despesas, builder.salario,
                builder.cargo, builder.regime, builder.departamento,
                builder.caminhoFoto
        );
    }

    public Gestor() {}

    public static class GestorBuilder {
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

        public GestorBuilder caminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; return this; }
        public GestorBuilder regime(Regime regime) { this.regime = regime; return this; }
        public GestorBuilder cargo(String cargo) { this.cargo = cargo; return this; }
        public GestorBuilder matricula(int matricula) { this.matricula = matricula; return this; }
        public GestorBuilder periodo(int periodo) { this.periodo = periodo; return this; }
        public GestorBuilder receita(double receita) { this.receita = receita; return this; }
        public GestorBuilder despesas(double despesas) { this.despesas = despesas; return this; }
        public GestorBuilder salario(double salario) { this.salario = salario; return this; }
        public GestorBuilder nome(String nome) { this.nome = nome; return this; }
        public GestorBuilder cpf(String cpf) { this.cpf = cpf; return this; }
        public GestorBuilder email(String email) { this.email = email; return this; }
        public GestorBuilder endereco(Endereco endereco) { this.endereco = endereco; return this; }
        public GestorBuilder login(String login) { this.login = login; return this; }
        public GestorBuilder senha(String senha) { this.senha = senha; return this; }
        public GestorBuilder perfil(Perfil perfil) { this.perfil = perfil; return this; }
        public GestorBuilder departamento(String departamento) { this.departamento = departamento; return this; }

        public Gestor build() {
            return new Gestor(this);
        }
    }
}