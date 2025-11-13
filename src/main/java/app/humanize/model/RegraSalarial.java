package app.humanize.model;

import javafx.beans.property.*;

public class RegraSalarial {

    private final StringProperty cargo;
    private final StringProperty nivel;
    private final DoubleProperty salarioBase;
    private final DoubleProperty adicionalNivel;
    private final DoubleProperty beneficios;
    private final DoubleProperty salarioTotal;

    public RegraSalarial(String cargo, String nivel, double salarioBase,
                         double adicionalNivel, double beneficios, double salarioTotal) {
        this.cargo = new SimpleStringProperty(cargo);
        this.nivel = new SimpleStringProperty(nivel);
        this.salarioBase = new SimpleDoubleProperty(salarioBase);
        this.adicionalNivel = new SimpleDoubleProperty(adicionalNivel);
        this.beneficios = new SimpleDoubleProperty(beneficios);
        this.salarioTotal = new SimpleDoubleProperty(salarioTotal);
    }

    public String getCargo() { return cargo.get(); }
    public void setCargo(String cargo) { this.cargo.set(cargo); }

    public String getNivel() { return nivel.get(); }
    public void setNivel(String nivel) { this.nivel.set(nivel); }


    public double getSalarioBase() { return salarioBase.get(); }

    public double getAdicionalNivel() { return adicionalNivel.get(); }

    public double getBeneficios() { return beneficios.get(); }
    public void setBeneficios(double beneficios) { this.beneficios.set(beneficios); }

    public double getSalarioTotal() { return salarioTotal.get(); }

    @Override
    public String toString() {
        return String.format("RegraSalarial{cargo='%s', nivel='%s', salarioTotal=%.2f}",
                getCargo(), getNivel(), getSalarioTotal());
    }
}