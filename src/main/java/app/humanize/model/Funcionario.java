package app.humanize.model;

import java.time.LocalDate;

public class Funcionario extends Usuario {
    private int matricula;
    private LocalDate dataAdmissao; // NOVO CAMPO
    private int periodo;
    private double receita;
    private double despesas;
    private double salario;
    private String cargo;
    private Regime regime;
    private String departamento;
    private String caminhoFoto;

    // construtor protected para ser usado por Builders das classes filhas
    protected Funcionario(String idiomaPreferencial, String nome, String cpf, Endereco endereco, String email, String login, String senha, Perfil perfil,
                          int matricula, LocalDate dataAdmissao, int periodo, double receita, double despesas, double salario,
                          String cargo, Regime regime, String departamento, String caminhoFoto) {

        super(nome, cpf, endereco, email, login, senha, perfil, idiomaPreferencial);
        this.matricula = matricula;
        this.dataAdmissao = dataAdmissao; // NOVO CAMPO
        this.periodo = periodo;
        this.departamento = departamento;
        this.receita = receita;
        this.despesas = despesas;
        this.salario = salario;
        this.cargo = cargo;
        this.regime = regime;
        this.caminhoFoto = caminhoFoto;
    }

    public Funcionario() {}

    // builder
    public static class FuncionarioBuilder {
        private int matricula;
        private LocalDate dataAdmissao; // NOVO CAMPO
        private int periodo;
        private double receita;
        private double despesas;
        private double salario;
        private String cargo;
        private Regime regime;
        private String departamento;
        private String caminhoFoto;
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;
        private String idiomaPreferencial;

        public FuncionarioBuilder caminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; return this; }
        public FuncionarioBuilder regime(Regime regime) { this.regime = regime; return this; }
        public FuncionarioBuilder cargo(String cargo) { this.cargo = cargo; return this; }
        public FuncionarioBuilder matricula(int matricula) { this.matricula = matricula; return this; }
        public FuncionarioBuilder dataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; return this; } // NOVO CAMPO
        public FuncionarioBuilder periodo(int periodo) { this.periodo = periodo; return this; }
        public FuncionarioBuilder departamento(String departamento) { this.departamento = departamento; return this; }
        public FuncionarioBuilder receita(double receita) { this.receita = receita; return this; }
        public FuncionarioBuilder despesas(double despesas) { this.despesas = despesas; return this; }
        public FuncionarioBuilder salario(double salario) { this.salario = salario; return this; }
        public FuncionarioBuilder nome(String nome) { this.nome = nome; return this; }
        public FuncionarioBuilder cpf(String cpf) { this.cpf = cpf; return this; }
        public FuncionarioBuilder email(String email) { this.email = email; return this; }
        public FuncionarioBuilder endereco(Endereco endereco) { this.endereco = endereco; return this; }
        public FuncionarioBuilder login(String login) { this.login = login; return this; }
        public FuncionarioBuilder senha(String senha) { this.senha = senha; return this; }
        public FuncionarioBuilder perfil(Perfil perfil) { this.perfil = perfil; return this; }
        public FuncionarioBuilder idiomaPreferencial(String idiomaPreferencial) { this.idiomaPreferencial = idiomaPreferencial; return this; }


        public Funcionario build() {
            return new Funcionario(
                    idiomaPreferencial, nome, cpf, endereco, email, login, senha, perfil,
                    matricula, dataAdmissao, periodo, receita, despesas, salario, cargo, regime, departamento,
                    caminhoFoto
            );
        }
    }

    public int getMatricula() { return matricula; }
    public void setMatricula(int matricula) { this.matricula = matricula; }
    public LocalDate getDataAdmissao() { return dataAdmissao; } // NOVO CAMPO
    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; } // NOVO CAMPO
    public int getPeriodo() { return periodo; }
    public void setPeriodo(int periodo) { this.periodo = periodo; }
    public double getReceita() { return receita; }
    public void setReceita(double receita) { this.receita = receita; }
    public double getDespesas() { return despesas; }
    public void setDespesas(double despesas) { this.despesas = despesas; }
    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public Regime getRegime() { return regime; }
    public void setRegime(Regime regime) { this.regime = regime; }
    public double getSaldo() { return (this.receita + this.salario) - this.despesas; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getCaminhoFoto() { return caminhoFoto; }
    public void setCaminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; }
}