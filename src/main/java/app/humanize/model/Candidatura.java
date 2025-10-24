package app.humanize.model;

import java.time.LocalDate;

public class Candidatura {
    private Candidato candidato;
    private Vaga vaga;
    private StatusCandidatura status;
    private LocalDate dataCandidatura;

    //construtor


    public Candidatura(Candidato candidato, LocalDate dataCandidatura, StatusCandidatura status, Vaga vaga) {
        this.candidato = candidato;
        this.dataCandidatura = dataCandidatura;
        this.status = status;
        this.vaga = vaga;
    }

    public Candidatura() {
    }

    //metodos especiais


    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public StatusCandidatura getStatus() {
        return status;
    }

    public void setStatus(StatusCandidatura status) {
        this.status = status;
    }

    public LocalDate getDataCandidatura() {
        return dataCandidatura;
    }

    public void setDataCandidatura(LocalDate dataCandidatura) {
        this.dataCandidatura = dataCandidatura;
    }
}
