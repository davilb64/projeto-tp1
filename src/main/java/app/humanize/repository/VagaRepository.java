package app.humanize.repository;

import app.humanize.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<Vaga> getVagasAbertas() {
        List<Vaga> vagasAbertas = new ArrayList<>();
        for (Vaga vaga : this.vagaEmMemoria) {
            if (vaga.getStatus() == StatusVaga.ABERTA) {
                vagasAbertas.add(vaga);
            }
        }
        return vagasAbertas;
    }

    public List<String> getTodosCargos() {
        List<String> cargos = new ArrayList<>();
        for(Vaga vaga : this.vagaEmMemoria) {
            cargos.add(vaga.getCargo());
        }
        return cargos;
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

    public void carregarVagaDoCSV() {
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
            escritor.write("ID;Cargo;Salario;Status;Requisitos;Departamento;DataVaga;IdRecrutador;NomeRecrutador;CPFRecrutador;\n");
            for (Vaga vaga : this.vagaEmMemoria) {
                escritor.write(formatarVagaParaCSV(vaga));
            }
        }
    }

    private Vaga parseVagaDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 9) return null;

        try {
            int id = Integer.parseInt(campos[0]);

            Vaga vaga = new Vaga();

            vaga.setId(id);
            vaga.setCargo(campos[1]);
            vaga.setSalario(campos[2]);
            vaga.setStatus(StatusVaga.valueOf(campos[3]));
            vaga.setRequisitos(campos[4]);
            vaga.setDepartamento(campos[5]);
            vaga.setDataVaga(campos[6] != null && !campos[6].isEmpty() ? LocalDate.parse(campos[6]) : null);

            if(campos[7] != null && !campos[7].isEmpty())
            {
                int idRecrutador = Integer.parseInt(campos[7]);
                String nomeRecrutador = campos[8];
                String cpfRecrutador = campos[9];

                Usuario usuario =  new Recrutador.RecrutadorBuilder()
                        .nome(nomeRecrutador).cpf(cpfRecrutador)
                        .build();
                usuario.setId(idRecrutador);

                vaga.setRecrutador(usuario);
            }
            return vaga;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            return null;
        }
    }

    private String formatarVagaParaCSV(Vaga vaga) {
        StringBuilder sb = new StringBuilder();
        sb.append(vaga.getId()).append(";");
        sb.append(vaga.getCargo() == null ? "" : vaga.getCargo()).append(";");
        sb.append(vaga.getSalario() == null ? "" : vaga.getSalario()).append(";");
        sb.append(vaga.getStatus() == null ? "" : vaga.getStatus()).append(";");
        sb.append(vaga.getRequisitos() == null ? "" : vaga.getRequisitos()).append(";");
        sb.append(vaga.getDepartamento() == null ? "" : vaga.getDepartamento()).append(";");
        sb.append(vaga.getDataVaga() == null ? "" : vaga.getDataVaga()).append(";");
        sb.append(vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getId()).append(";");
        sb.append(vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getNome()).append(";");
        sb.append(vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getCpf()).append(";");
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
        //this.vagaEmMemoria.replaceAll(v -> v.getId() == vaga.getId() ? vaga : v);
        persistirAlteracoesNoCSV();
    }
}