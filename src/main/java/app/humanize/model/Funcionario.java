package app.humanize.model;

import java.time.LocalDate;

public class Funcionario extends Usuario{
    private int matricula;
    private int periodo;
    private LocalDate dataEmissao;
    private double receita;
    private double despesas;
    private double salario;
    private double saldo;

    //construtores

    public Funcionario(String nome, String cpf, Endereco endereco, String email, String login, String senha, int matricula, int periodo, LocalDate dataEmissao, double receita, double despesas, double salario, double saldo) {
        super(nome, cpf, endereco, email, login, senha);
        this.matricula = matricula;
        this.periodo = periodo;
        this.dataEmissao = dataEmissao;
        this.receita = receita;
        this.despesas = despesas;
        this.salario = salario;
        this.saldo = saldo;
    }
    public Funcionario() {}

}
