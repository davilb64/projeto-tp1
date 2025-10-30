package app.humanize.repository;

import app.humanize.model.*;
import javafx.scene.control.Alert;

import java.io.*;
import java.time.LocalDate;
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

    public List<Candidatura> getTodas() {
        return new ArrayList<>(candidaturasEmMemoria);
    }

    public List<Candidatura> getCandidaturasEmAnalise() {
        List<Candidatura> candidaturas = new ArrayList<>();
        for(Candidatura candidatura : candidaturasEmMemoria) {
            if (candidatura.getStatus() == StatusCandidatura.EM_ANALISE){
                candidaturas.add(candidatura);
            }
        }
        return candidaturas;
    }

    /** Verifica se já existe uma candidatura do mesmo candidato para a mesma vaga */
    public boolean existeCandidatura(Candidato candidato, Vaga vaga) {
        return candidaturasEmMemoria.stream()
                .anyMatch(c ->
                        c.getCandidato().getCpf().equals(candidato.getCpf()) &&
                                c.getVaga().getId() == vaga.getId()
                );
    }

    /** Salva uma nova candidatura no CSV, evitando duplicatas */
    public void salvar(Candidatura candidatura) throws IOException {
        if (candidatura.getCandidato() == null || candidatura.getVaga() == null) {
            mostrarErro("Candidato ou Vaga inválidos.");
            return;
        }

        if (existeCandidatura(candidatura.getCandidato(), candidatura.getVaga())) {
            mostrarErro("Este candidato já está inscrito nesta vaga.");
            return;
        }

        candidaturasEmMemoria.add(candidatura);
        persistirAlteracoesNoCSV();
    }

    public void remover(Candidatura candidatura) throws IOException {
        candidaturasEmMemoria.removeIf(c ->
                c.getCandidato().getCpf().equals(candidatura.getCandidato().getCpf()) &&
                        c.getVaga().getId() == candidatura.getVaga().getId());
        persistirAlteracoesNoCSV();
    }


    /** Persiste todas as candidaturas no CSV */
    private void persistirAlteracoesNoCSV() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoCsv, false))) {
            writer.write("CPF_Candidato;Nome_Candidato;Vaga_ID;Cargo;Data;Status\n");
            for (Candidatura c : candidaturasEmMemoria) {
                writer.write(formatarCandidaturaParaCSV(c));
            }
        }
    }

    /** Lê candidaturas do CSV para a memória */
    private void carregarCandidaturasDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            reader.readLine(); // Pular cabeçalho
            String linha;
            while ((linha = reader.readLine()) != null) {
                Candidatura c = parseLinhaCSV(linha);
                if (c != null) candidaturasEmMemoria.add(c);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar candidaturas do CSV: " + e.getMessage());
        }
    }

    /** Converte uma linha CSV para um objeto Candidatura */
    private Candidatura parseLinhaCSV(String linha) {
        try {
            String[] campos = linha.split(";", -1);
            if (campos.length < 6) return null;

            String cpf = campos[0];
            String nome = campos[1];
            int idVaga = Integer.parseInt(campos[2]);
            String cargo = campos[3];
            LocalDate data = campos[4].isEmpty() ? LocalDate.now() : LocalDate.parse(campos[4]);
            StatusCandidatura status = StatusCandidatura.valueOf(campos[5]);

            Candidato candidato = new Candidato.CandidatoBuilder()
                    .cpf(cpf)
                    .nome(nome)
                    .build();

            Vaga vaga = new Vaga();
            vaga.setId(idVaga);
            vaga.setCargo(cargo);

            return new Candidatura(candidato, data, status, vaga);

        } catch (Exception e) {
            System.err.println("Erro ao parsear linha: " + linha + " → " + e.getMessage());
            return null;
        }
    }

    /** Converte uma Candidatura para linha CSV */
    private String formatarCandidaturaParaCSV(Candidatura c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getCandidato().getCpf()).append(";");
        sb.append(c.getCandidato().getNome()).append(";");
        sb.append(c.getVaga().getId()).append(";");
        sb.append(c.getVaga().getCargo()).append(";");
        sb.append(c.getDataCandidatura() != null ? c.getDataCandidatura() : LocalDate.now()).append(";");
        sb.append(c.getStatus() != null ? c.getStatus() : StatusCandidatura.EM_ANALISE).append("\n");
        return sb.toString();
    }

    public String getStatusPorCandidato(Candidato candidato) {
        return candidaturasEmMemoria.stream()
                .filter(c -> c.getCandidato().equals(candidato))
                .findFirst()
                .map(c -> {
                    if (c.getStatus() == null) return "Em análise";
                    switch (c.getStatus()) {
                        case APROVADO: return "Aprovado";
                        case REPROVADO: return "Reprovado";
                        case PENDENTE: return "Em análise";
                        default: return "Em análise";
                    }
                })
                .orElse("Sem candidatura");
    }



    /** Mostra alerta de erro visual no JavaFX */
    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
