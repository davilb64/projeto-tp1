package app.humanize.model.factories;

public class FolhaPag {
    private String nome;
    private String cargo;
    private String nivel;
    private double salarioBase;
    private double adicionalNivel;
    private double beneficios;
    private double adicionais;
    private double descontos;
    private double salarioLiquido;

    public FolhaPag() {}

    public FolhaPag(String nome, String cargo, String nivel,
                    double salarioBase, double adicionalNivel, double beneficios,
                    double adicionais, double descontos, double salarioLiquido) {

        this.nome = nome;
        this.cargo = cargo;
        this.nivel = nivel;
        this.salarioBase = salarioBase;
        this.adicionalNivel = adicionalNivel;
        this.beneficios = beneficios;
        this.adicionais = adicionais;
        this.descontos = descontos;
        this.salarioLiquido = salarioLiquido;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public double getSalarioBase() { return salarioBase; }
    public void setSalarioBase(double salarioBase) { this.salarioBase = salarioBase; }

    public double getAdicionalNivel() { return adicionalNivel; }
    public void setAdicionalNivel(double adicionalNivel) { this.adicionalNivel = adicionalNivel; }

    public double getBeneficios() { return beneficios; }
    public void setBeneficios(double beneficios) { this.beneficios = beneficios; }

    public double getAdicionais() { return adicionais; }
    public void setAdicionais(double adicionais) { this.adicionais = adicionais; }

    public double getDescontos() { return descontos; }
    public void setDescontos(double descontos) { this.descontos = descontos; }

    public double getSalarioLiquido() { return salarioLiquido; }
    public void setSalarioLiquido(double salarioLiquido) { this.salarioLiquido = salarioLiquido; }
}
