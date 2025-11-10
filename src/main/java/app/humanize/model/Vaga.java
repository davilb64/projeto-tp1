package app.humanize.model;

import java.time.LocalDate;


public class Vaga {
    private static int idCounter = 0;
    private int id;
    private String cargo;

    private String salario;
    private String requisitos;
    private String departamento;
    private LocalDate dataVaga;
    private StatusVaga status;
    private Usuario recrutador;

    public Vaga(){}

    public Vaga(String cargo, StatusVaga status, String salario, String requisitos, String departamento, Usuario recrutador){
        this.id = ++idCounter;
        this.cargo = cargo;
        this.status = status;
        this.salario = salario;
        this.requisitos = requisitos;
        this.departamento = departamento;
        this.recrutador = recrutador;
    }

    public Vaga(String text, StatusVaga statusVaga, String text1, String text2, String text3) {
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

    public StatusVaga getStatus() {
        return status;
    }

    public void setStatus(StatusVaga status) {
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

    public Usuario getRecrutador() { return recrutador; }

    public void setRecrutador(Usuario recrutador) { this.recrutador = recrutador; }



    // toString() define o texto exibido no ChoiceBox
    @Override
    public String toString() {
        return id + " - " + cargo + " - " + status + " - " + salario + " - " + requisitos;
    }

}
