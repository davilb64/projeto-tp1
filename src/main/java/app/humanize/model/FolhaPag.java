package app.humanize.model;

import java.time.LocalDate;

public class FolhaPag {
    private String nome;
    private String cargo;
    private String nivel;
    private final double salarioBase;
    private final double adicionalNivel;
    private double beneficios;
    private final double adicionais;
    private final double descontos;
    private final double salarioLiquido;
    private LocalDate data;

    public FolhaPag(String nome, String cargo, String nivel,
                    double salarioBase, double adicionalNivel, double beneficios,
                    double adicionais, double descontos, double salarioLiquido, LocalDate data) {
        this.nome = nome;
        this.cargo = cargo;
        this.nivel = nivel;
        this.salarioBase = salarioBase;
        this.adicionalNivel = adicionalNivel;
        this.beneficios = beneficios;
        this.adicionais = adicionais;
        this.descontos = descontos;
        this.salarioLiquido = salarioLiquido;
        this.data = data;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public double getSalarioBase() { return salarioBase; }

    public double getAdicionalNivel() { return adicionalNivel; }

    public double getBeneficios() { return beneficios; }
    public void setBeneficios(double beneficios) { this.beneficios = beneficios; }

    public double getAdicionais() { return adicionais; }

    public double getDescontos() { return descontos; }

    public double getSalarioLiquido() { return salarioLiquido; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getMesAno() {
        return data.toString();
    }
}