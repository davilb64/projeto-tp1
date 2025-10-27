package app.humanize.repository;

import app.humanize.model.Candidatura;
import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CandidaturaRepository {
    private static final CandidaturaRepository instance = new CandidaturaRepository();
    private final String arquivoCsv = "./src/main/resources/candidaturas.csv";
    private final List<Candidatura> candidaturasEmMemoria;

    private CandidaturaRepository() {
        this.candidaturasEmMemoria = new ArrayList<>();
        this.carregarCandidaturasDoCSV();
    }

    public static CandidaturaRepository getInstance() {
        return instance;
    }

    public boolean existeCandidatura(Candidato candidato, Vaga vaga) {
        return candidaturasEmMemoria.stream()
                .anyMatch(c -> c.getCandidato().equals(candidato) && c.getVaga().equals(vaga));
    }

    public void salvar(Candidatura candidatura) throws IOException {
        // Implementar lógica de salvar no CSV
        // Similar ao que você tem no VagaRepository
    }

    private void carregarCandidaturasDoCSV() {
        // Implementar carregamento do CSV
    }

    // Outros métodos necessários...
}