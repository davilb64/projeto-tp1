package app.humanize.model;

//to pondo isso pra poder testar, se apagar me avisa :) -kiki

import java.time.LocalDate;


public class Vaga {
    private String cargo;
    private String salario;
    private String requisitos;
    private String departamento;
    private LocalDate dataVaga;

    public void criar(String nome, String numero){
        this.cargo = nome;
        this.salario = numero;
    }


    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSalario() {
        return salario;
    }

    public void setSalario(String salario) {
        this.salario = salario;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    // toString() define o texto exibido no ChoiceBox
    @Override
    public String toString() {
        return cargo + salario;
    }
}
