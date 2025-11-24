package app.humanize.repository;

import app.humanize.model.Candidato;
import app.humanize.model.Candidatura;
import app.humanize.model.StatusCandidatura;
import app.humanize.model.Vaga;
import javafx.scene.control.Alert;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CandidaturaRepository extends BaseRepository {
    private static final CandidaturaRepository instance = new CandidaturaRepository();
    private static final String NOME_ARQUIVO = "candidaturas.csv";
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

    public List<Candidatura> getCandidaturasAprovadas() {
        List<Candidatura> candidaturas = new ArrayList<>();
        for (Candidatura candidatura: candidaturasEmMemoria) {
            if (candidatura.getStatus() == StatusCandidatura.APROVADO){
                candidaturas.add(candidatura);
            }
        }
        return candidaturas;
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

    public List<Candidato> getCandidatosAprovados() {
        List<Candidato> candidatos = new ArrayList<>();
        for (Candidatura candidatura: candidaturasEmMemoria) {
            if (candidatura.getStatus() == StatusCandidatura.APROVADO){
                candidatos.add(candidatura.getCandidato());
            }
        }
        return candidatos;
    }

    public Integer getIdVagaDoCandidatoAprovado(Candidato aprovado) {
        for (Candidatura candidatura: candidaturasEmMemoria) {
            if (candidatura.getStatus() == StatusCandidatura.APROVADO &&
                candidatura.getCandidato().getId() == aprovado.getId()) {
                return candidatura.getVaga().getId();
            }
        }
        return null;
    }

    public List<Candidatura> getCandidaturasPendentePorVaga(Vaga vaga) {
        List<Candidatura> candidaturas = new ArrayList<>();
        for(Candidatura candidatura : candidaturasEmMemoria) {
            if (candidatura.getStatus() == StatusCandidatura.PENDENTE && candidatura.getVaga().getId() == vaga.getId()) {
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

    public void removerPorCandidato(Candidato candidato) throws IOException {
        if (candidato == null || candidato.getCpf() == null) {
            return;
        }

        candidaturasEmMemoria.removeIf(c ->
                c.getCandidato().getCpf().equals(candidato.getCpf())
        );

        persistirAlteracoesNoCSV();
    }

    public void remover(Candidatura candidatura) throws IOException {
        candidaturasEmMemoria.removeIf(c ->
                c.getCandidato().getCpf().equals(candidatura.getCandidato().getCpf()) &&
                        c.getVaga().getCargo().equalsIgnoreCase(candidatura.getVaga().getCargo())
        );
        persistirAlteracoesNoCSV();
    }



    /** Persiste todas as candidaturas no CSV */
    private void persistirAlteracoesNoCSV() throws IOException {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, false))) {
            writer.write("CPF_Candidato;Nome_Candidato;Vaga_ID;Cargo;Data;Status\n");
            for (Candidatura c : candidaturasEmMemoria) {
                writer.write(formatarCandidaturaParaCSV(c));
            }
        }
    }

    /** Lê candidaturas do CSV para a memória */
    private void carregarCandidaturasDoCSV() {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        if (!arquivo.exists()){
            System.out.println("Arquivo " + NOME_ARQUIVO + " não encontrado. Copiando arquivo padrão...");
            try {
                copiarArquivoDefaultDeResources(NOME_ARQUIVO, arquivo);
            } catch (IOException e) {
                System.err.println("!!! FALHA CRÍTICA AO COPIAR ARQUIVO PADRÃO: " + NOME_ARQUIVO);
                e.printStackTrace();
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            reader.readLine();
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
        return c.getCandidato().getCpf() + ";" +
                c.getCandidato().getNome() + ";" +
                c.getVaga().getId() + ";" +
                c.getVaga().getCargo() + ";" +
                (c.getDataCandidatura() != null ? c.getDataCandidatura() : LocalDate.now()) + ";" +
                (c.getStatus() != null ? c.getStatus() : StatusCandidatura.EM_ANALISE) + "\n";
    }

    public void salvarOuAtualizar(Candidatura candidatura) throws IOException {
        for (Candidatura c : candidaturasEmMemoria) {
            if (c.getCandidato().equals(candidatura.getCandidato()) &&
                    c.getVaga().equals(candidatura.getVaga())) {
                c.setStatus(candidatura.getStatus());
                persistirAlteracoesNoCSV();
                return;
            }
        }
        candidaturasEmMemoria.add(candidatura);
        persistirAlteracoesNoCSV();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}