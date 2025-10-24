package app.humanize.model;

//to pondo isso pra poder testar, se apagar me avisa :) -kiki

import java.time.LocalDate;


public class Vaga {
    private static int idCounter = 0;
    private int id;
    private String cargo;
    private String status;
    private String salario;
    private String requisitos;
    private String departamento;
    private LocalDate dataVaga;

    public Vaga(){}

    public Vaga(String cargo, String status, String salario, String requisitos){
        this.id = ++idCounter;
        this.cargo = cargo;
        this.status = status;
        this.salario = salario;
        this.requisitos = requisitos;
    }
    public void criar(String cargo, String salario){
        this.id = ++idCounter;
        this.cargo = cargo;
        this.salario = salario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDate getDataVaga() {
        return dataVaga;
    }

    public void setDataVaga(LocalDate dataVaga) {
        this.dataVaga = dataVaga;
    }

    // toString() define o texto exibido no ChoiceBox
    @Override
    public String toString() {
        return id + " - " + cargo + " - " + status + " - " + salario + " - " + requisitos;
    }

}
