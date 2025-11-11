package app.humanize.model;

import java.time.LocalDate;

public class Entrevista {
    private static int idCounter = 0;
    private int id;
    private Usuario recrutador;
    private Vaga vaga;
    private Candidatura candidatura;
    private StatusEntrevista status;
    private LocalDate dataEntrevista;

    public Entrevista() {}

    public Entrevista(Usuario recrutador, Vaga vaga, Candidatura candidatura, StatusEntrevista status, LocalDate dataEntrevista) {
        this.id = ++idCounter;
        this.recrutador = recrutador;
        this.vaga = vaga;
        this.candidatura = candidatura;
        this.status = status;
        this.dataEntrevista = dataEntrevista;
    }

    public Usuario getRecrutador() {
        return recrutador;
    }

    public void setRecrutador(Usuario recrutador) {
        this.recrutador = recrutador;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public Candidatura getCandidatura() {
        return candidatura;
    }

    public void setCandidatura(Candidatura candidatura) {
        this.candidatura = candidatura;
    }

    public StatusEntrevista getStatus() {
        return status;
    }

    public void setStatus(StatusEntrevista status) {
        this.status = status;
    }

    public LocalDate getDataEntrevista() {
        return dataEntrevista;
    }

    public void setDataEntrevista(LocalDate dataEntrevista) {
        this.dataEntrevista = dataEntrevista;
    }

    @Override
    public String toString() {
        return id + " - " + candidatura.getCandidato().getNome();
    }

}
