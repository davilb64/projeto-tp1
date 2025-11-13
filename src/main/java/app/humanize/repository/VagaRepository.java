package app.humanize.repository;

import app.humanize.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VagaRepository extends BaseRepository {

    private static final VagaRepository instance = new VagaRepository();

    private static final String NOME_ARQUIVO_CSV = "vagas.csv";
    private final File arquivoDePersistencia;

    private final List<Vaga> vagaEmMemoria;

    private VagaRepository() {
        this.arquivoDePersistencia = this.getArquivoDePersistencia(NOME_ARQUIVO_CSV);
        this.vagaEmMemoria = new ArrayList<>();

        try {
            if (!this.arquivoDePersistencia.exists()) {
                this.copiarArquivoDefaultDeResources(NOME_ARQUIVO_CSV, this.arquivoDePersistencia);
            }
        } catch (IOException e) {
            System.err.println("Erro CRÍTICO ao copiar arquivo de seeding: " + e.getMessage());
        }

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

    public List<Vaga> getVagasAbertasPorRecrutador(Usuario recrutador) {
        List<Vaga> vagasAbertas = new ArrayList<>();
        for (Vaga vaga : this.vagaEmMemoria) {
            if (vaga.getStatus() == StatusVaga.ABERTA && vaga.getRecrutador() != null && vaga.getRecrutador().getId() == recrutador.getId()) {
                vagasAbertas.add(vaga);
            }
        }
        return vagasAbertas;
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
        File arquivo = this.arquivoDePersistencia;

        if (!arquivo.exists()) {
            System.err.println("Arquivo de persistência não encontrado (deveria ter sido criado): " + arquivo.getAbsolutePath());
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
        try (FileWriter escritor = new FileWriter(this.arquivoDePersistencia, false)) {
            escritor.write("ID;Cargo;Salario;Status;Requisitos;Departamento;DataVaga;IdPessoa;NomePessoa;CpfPessoa;PerfilPessoa\n");
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
            vaga.setStatus(StatusVaga.valueOf(campos[3]));
            vaga.setRequisitos(campos[4]);
            vaga.setDepartamento(campos[5]);
            vaga.setDataVaga(campos[6] != null && !campos[6].isEmpty() ? LocalDate.parse(campos[6]) : null);

            if(campos.length >= 10){
                if(campos[7] != null && !campos[7].isEmpty()){
                    Usuario recrutador = new Recrutador.RecrutadorBuilder().build();
                    int idPessoa = Integer.parseInt(campos[7]);
                    recrutador.setId(idPessoa);
                    recrutador.setNome(campos[8]);
                    recrutador.setCpf(campos[9]);
                    recrutador.setPerfil(Perfil.valueOf(campos[10]));
                    vaga.setRecrutador(recrutador);
                }
            }
            return vaga;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            return null;
        }
    }

    private String formatarVagaParaCSV(Vaga vaga) {
        return vaga.getId() + ";" +
                (vaga.getCargo() == null ? "" : vaga.getCargo()) + ";" +
                (vaga.getSalario() == null ? "" : vaga.getSalario()) + ";" +
                (vaga.getStatus() == null ? "" : vaga.getStatus()) + ";" +
                (vaga.getRequisitos() == null ? "" : vaga.getRequisitos()) + ";" +
                (vaga.getDepartamento() == null ? "" : vaga.getDepartamento()) + ";" +
                (vaga.getDataVaga() == null ? "" : vaga.getDataVaga()) + ";" +
                (vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getId()) + ";" +
                (vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getNome()) + ";" +
                (vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getCpf()) + ";" +
                (vaga.getRecrutador() == null ? "" : vaga.getRecrutador().getPerfil()) + ";" +
                "\n";
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