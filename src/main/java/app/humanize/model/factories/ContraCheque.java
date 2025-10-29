package app.humanize.model;

import java.time.LocalDate;

public class ContraCheque {
    private String nomeFuncionario;
    private LocalDate dataEmissao;
    private double totalProventos;
    private double totalDescontos;
    private double saldo;

    public ContraCheque() {}

    public ContraCheque(String nomeFuncionario, LocalDate dataEmissao, double totalProventos, double totalDescontos, double saldo) {
        this.nomeFuncionario = nomeFuncionario;
        this.dataEmissao = dataEmissao;
        this.totalProventos = totalProventos;
        this.totalDescontos = totalDescontos;
        this.saldo = saldo;
    }

    public String getNomeFuncionario() { return nomeFuncionario; }
    public void setNomeFuncionario(String nomeFuncionario) { this.nomeFuncionario = nomeFuncionario; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public double getTotalProventos() { return totalProventos; }
    public void setTotalProventos(double totalProventos) { this.totalProventos = totalProventos; }

    public double getTotalDescontos() { return totalDescontos; }
    public void setTotalDescontos(double totalDescontos) { this.totalDescontos = totalDescontos; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}