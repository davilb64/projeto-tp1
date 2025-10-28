package app.humanize.model;

import java.time.LocalDate;

public class Entrevista {
    private static int idCounter = 0;
    private int id;
    private Usuario recrutador;
    private Vaga vaga;
    private Candidato candidato;
    private StatusEntrevista status;
    private LocalDate dataEntrevista;

    public Entrevista() {}

    public Entrevista(Usuario recrutador, Vaga vaga, Candidato candidato, StatusEntrevista status, LocalDate dataEntrevista) {
        this.id = ++idCounter;
        this.recrutador = recrutador;
        this.vaga = vaga;
        this.candidato = candidato;
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

    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
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
}
