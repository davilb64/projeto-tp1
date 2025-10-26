package app.humanize.model;

import java.time.LocalDate;

public class Contratacao {

    private static int idCounter = 0;
    private int id;
    private Candidato candidato;
    private Vaga vaga;
    private LocalDate dataContratacao;
    private String regime; // CLT, Estágio, PJ, etc.

    public Contratacao() {}

    public Contratacao(Candidato candidato, Vaga vaga,
                       LocalDate dataContratacao, String regime) {
        this.id = ++idCounter;
        this.candidato = candidato;
        this.vaga = vaga;
        this.dataContratacao = dataContratacao;
        this.regime = regime;
    }

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) {  this.id = id; }
    public Candidato getCandidato() { return candidato; }
    public void setCandidato(Candidato candidato) { this.candidato = candidato; }

    public Vaga getVaga() { return vaga; }
    public void setVaga(Vaga vaga) { this.vaga = vaga; }

    public LocalDate getDataContratacao() { return dataContratacao; }
    public void setDataContratacao(LocalDate dataContratacao) { this.dataContratacao = dataContratacao; }

    public String getRegime() { return regime; }
    public void setRegime(String regime) { this.regime = regime; }

    @Override
    public String toString() {
        return "Contratação #" + id + " - " + candidato.getNome() +
                " (" + regime + ") para vaga: " + vaga.getCargo();
    }
}
