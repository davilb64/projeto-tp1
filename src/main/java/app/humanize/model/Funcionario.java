package app.humanize.model;

import java.time.LocalDate;

public class Funcionario extends Usuario {
    private int matricula;
    private int periodo;
    private LocalDate dataEmissao;
    private double receita;
    private double despesas;
    private double salario;
    private String cargo;
    private Regime regime;

    // builder
    public static class FuncionarioBuilder {
        private int matricula;
        private int periodo;
        private LocalDate dataEmissao;
        private double receita;
        private double despesas;
        private double salario;
        private String cargo;
        private Regime regime;

        // Atributos de Usuario/Pessoa
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;

        public FuncionarioBuilder regime(Regime regime) {
            this.regime = regime;
            return this;
        }

        public FuncionarioBuilder cargo(String cargo) {
            this.cargo = cargo;
            return this;
        }

        public FuncionarioBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public FuncionarioBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public FuncionarioBuilder email(String email) {
            this.email = email;
            return this;
        }

        public FuncionarioBuilder endereco(Endereco endereco) {
            this.endereco = endereco;
            return this;
        }

        public FuncionarioBuilder login(String login) {
            this.login = login;
            return this;
        }

        public FuncionarioBuilder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public FuncionarioBuilder perfil(Perfil perfil) {
            this.perfil = perfil;
            return this;
        }

        public FuncionarioBuilder matricula(int matricula) {
            this.matricula = matricula;
            return this;
        }

        public FuncionarioBuilder periodo(int periodo) {
            this.periodo = periodo;
            return this;
        }

        public FuncionarioBuilder dataEmissao(LocalDate dataEmissao) {
            this.dataEmissao = dataEmissao;
            return this;
        }

        public FuncionarioBuilder receita(double receita) {
            this.receita = receita;
            return this;
        }

        public FuncionarioBuilder despesas(double despesas) {
            this.despesas = despesas;
            return this;
        }

        public FuncionarioBuilder salario(double salario) {
            this.salario = salario;
            return this;
        }

        public Funcionario build() {
            return new Funcionario(this);
        }
    }

    // construtor
    private Funcionario(FuncionarioBuilder builder) {
        super(builder.nome, builder.cpf, builder.endereco, builder.email, builder.login, builder.senha, builder.perfil);
        this.matricula = builder.matricula;
        this.periodo = builder.periodo;
        this.dataEmissao = builder.dataEmissao;
        this.receita = builder.receita;
        this.despesas = builder.despesas;
        this.salario = builder.salario;
        this.cargo = builder.cargo;
        this.regime = builder.regime;
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public double getReceita() {
        return receita;
    }

    public void setReceita(double receita) {
        this.receita = receita;
    }

    public double getDespesas() {
        return despesas;
    }

    public void setDespesas(double despesas) {
        this.despesas = despesas;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public double getSaldo() {
        return (this.receita + this.salario) - this.despesas;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Regime getRegime() {
        return regime;
    }

    public void setRegime(Regime regime) {
        this.regime = regime;
    }
}