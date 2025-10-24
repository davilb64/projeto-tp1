package app.humanize.repository;

import app.humanize.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VagaRepository {

    private static final VagaRepository instance = new VagaRepository();
    private final String arquivoCsv = "./src/main/resources/vagas.csv";
    private final List<Vaga> vagaEmMemoria;

    private VagaRepository() {
        this.vagaEmMemoria = new ArrayList<>();
        this.carregarVagaDoCSV();
    }

    public static VagaRepository getInstance() {
        return instance;
    }

    public List<Vaga> getTodasVagas() {
        return new ArrayList<>(this.vagaEmMemoria);
    }

    public int getQtdVaga(){
        return this.vagaEmMemoria.size();
    }

    public void escreveVagaNova(Vaga vaga) throws IOException {
        int proximoId = getProximoId();
        vaga.setId(proximoId);

        this.vagaEmMemoria.add(vaga);
        this.persistirAlteracoesNoCSV();
    }

    public int getProximoId() {
        return this.vagaEmMemoria.stream()
                .mapToInt(Vaga::getId)
                .max()
                .orElse(0)
                + 1;
    }

    private void carregarVagaDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) {
            return;
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine(); // Pula o cabeçalho
            String linha;
            while ((linha = leitor.readLine()) != null) {
                Vaga vaga = parseVagaDaLinhaCsv(linha);
                if (vaga != null) {
                    this.vagaEmMemoria.add(vaga);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar vagas do arquivo CSV: " + e.getMessage());
        }
    }

    private void persistirAlteracoesNoCSV() throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("ID;Cargo;Salario;Status;Requisitos;Departamento;DataVaga;\n");
            for (Vaga vaga : this.vagaEmMemoria) {
                escritor.write(formatarVagaParaCSV(vaga));
            }
        }
    }

    private Vaga parseVagaDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 6) return null;

        try {
            int id = Integer.parseInt(campos[0]);


            Vaga vaga = new Vaga();

            vaga.setId(id);
            vaga.setCargo(campos[1]);
            vaga.setSalario(campos[2]);
            vaga.setStatus(campos[3]);
            vaga.setRequisitos(campos[4]);
            vaga.setDepartamento(campos[5]);
            vaga.setDataVaga(LocalDate.parse(campos[6]));

            return vaga;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            return null;
        }
    }

    private String formatarVagaParaCSV(Vaga vaga) {
        StringBuilder sb = new StringBuilder();
        sb.append(vaga.getId()).append(";");
        sb.append(vaga.getCargo()).append(";");
        sb.append(vaga.getSalario()).append(";");
        sb.append(vaga.getStatus()).append(";");
        sb.append(vaga.getRequisitos()).append(";");
        sb.append(vaga.getDepartamento()).append(";");
        sb.append(vaga.getDataVaga()).append(";");
        sb.append("\n");
        return sb.toString();
    }

    public void excluirVaga(Vaga vagaParaExcluir) throws IOException {
        if (vagaParaExcluir == null) {
            return;
        }
        boolean removido = this.vagaEmMemoria.removeIf(vaga -> vaga.getId() == vagaParaExcluir.getId());
        if (removido) {
            persistirAlteracoesNoCSV();
        }
    }

    public void atualizarVaga() throws IOException {
        persistirAlteracoesNoCSV();
    }
}