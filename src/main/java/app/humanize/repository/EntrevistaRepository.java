package app.humanize.repository;

import app.humanize.model.*;
import com.sun.javafx.scene.EnteredExitedHandler;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EntrevistaRepository {
    private static final EntrevistaRepository instance = new EntrevistaRepository();
    private final String arquivoCsv = "./src/main/resources/entrevistas.csv";
    private final List<Entrevista> entrevistaEmMemoria;

    private EntrevistaRepository() {
        this.entrevistaEmMemoria = new ArrayList<>();
        this.carregarEntrevistaDoCSV();
    }

    public static EntrevistaRepository getInstance() {
        return instance;
    }

    public List<Entrevista> getTodasEntrevistas() {
        return new ArrayList<>(this.entrevistaEmMemoria);
    }

    //metodos de salvar no csv
    public void escreveEntrevistaNova(Entrevista entrevista) throws IOException {
        int proximoId = getProximoId();
        entrevista.setId(proximoId);

        this.entrevistaEmMemoria.add(entrevista);
        this.persistirAlteracoesNoCSV();
    }
    public int getProximoId() {
        return this.entrevistaEmMemoria.stream()
                .mapToInt(Entrevista::getId)
                .max()
                .orElse(0)
                + 1;
    }
    private void persistirAlteracoesNoCSV() throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("idEntrevista;DataEntrevista;StatusEntrevista;Nome;CPF;Email;Telefone;Formacao;Disponibilidade;Pretencao;idVaga;Cargo;Salario;Status;Requisitos;Departamento;DataVaga;IdPessoa;NomePessoa;CpfPessoa;PerfilPessoa\n");
            for (Entrevista entrevista : this.entrevistaEmMemoria) {
                escritor.write(formatarEntrevistaParaCSV(entrevista));
            }
        }
    }

    private String formatarEntrevistaParaCSV(Entrevista entrevista) {
        StringBuilder sb = new StringBuilder();
        sb.append(entrevista.getId()).append(";");
        sb.append(entrevista.getDataEntrevista() == null ? "" : entrevista.getDataEntrevista()).append(";");
        sb.append(entrevista.getStatus() == null ? "" : entrevista.getStatus()).append(";");

        if(entrevista.getCandidato() != null){
            sb.append(entrevista.getCandidato().getNome() == null ? "" : entrevista.getCandidato().getNome()).append(";");
            sb.append(entrevista.getCandidato().getCpf() == null ? "" : entrevista.getCandidato().getCpf()).append(";");
            sb.append(entrevista.getCandidato().getEmail() == null ? "" : entrevista.getCandidato().getEmail()).append(";");
            sb.append(entrevista.getCandidato().getTelefone() == null ? "" : entrevista.getCandidato().getTelefone()).append(";");
            sb.append(entrevista.getCandidato().getFormacao() == null ? "" : entrevista.getCandidato().getFormacao()).append(";");
            sb.append(entrevista.getCandidato().getDisponibilidade() == null ? "" : entrevista.getCandidato().getDisponibilidade()).append(";");
            sb.append(String.valueOf(entrevista.getCandidato().getPretencaoSalarial())).append(";");
        }
        if(entrevista.getVaga() != null){
            sb.append(entrevista.getVaga().getId()).append(";");
            sb.append(entrevista.getVaga().getCargo() == null ? "" : entrevista.getVaga().getCargo()).append(";");
            sb.append(entrevista.getVaga().getSalario() == null ? "" : entrevista.getVaga().getSalario()).append(";");
            sb.append(entrevista.getVaga().getStatus() == null ? "" : entrevista.getVaga().getStatus()).append(";");
            sb.append(entrevista.getVaga().getRequisitos() == null ? "" : entrevista.getVaga().getRequisitos()).append(";");
            sb.append(entrevista.getVaga().getDepartamento() == null ? "" : entrevista.getVaga().getDepartamento()).append(";");
            sb.append(entrevista.getVaga().getDataVaga() == null ? "" : entrevista.getVaga().getDataVaga()).append(";");
        }
        if(entrevista.getRecrutador() != null){
            sb.append(entrevista.getRecrutador().getId()).append(";");
            sb.append(entrevista.getRecrutador().getNome() == null ? "" : entrevista.getRecrutador().getNome()).append(";");
            sb.append(entrevista.getRecrutador().getCpf() == null ? "" : entrevista.getRecrutador().getCpf()).append(";");
            sb.append(entrevista.getRecrutador().getPerfil() == null ? "" : entrevista.getRecrutador().getPerfil()).append(";");
        }
        sb.append("\n");
        return sb.toString();
    }

    //recuperar dados do arquivo csv
    public void carregarEntrevistaDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) {
            return;
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine(); // Pula o cabeçalho
            String linha;
            while ((linha = leitor.readLine()) != null) {
                Entrevista entrevista = parseEntrevistaDaLinhaCsv(linha);
                if (entrevista != null) {
                    this.entrevistaEmMemoria.add(entrevista);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar vagas do arquivo CSV: " + e.getMessage());
        }
    }

    private Entrevista parseEntrevistaDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 20) return null;

        try {
            int idContratacao = Integer.parseInt(campos[0]);

            Entrevista entrevista = new Entrevista();
            entrevista.setId(idContratacao);
            entrevista.setDataEntrevista(campos[1] != null && !campos[1].isEmpty() ? LocalDate.parse(campos[1]) : null);
            entrevista.setStatus(campos[2]);

            //dados do candidato
            Candidato candidato = new Candidato();
            candidato.setNome(campos[3]);
            candidato.setCpf(campos[4]);
            candidato.setEmail(campos[5]);
            candidato.setTelefone(campos[6]);
            candidato.setFormacao(campos[7]);
            candidato.setDisponibilidade(campos[8]);
            candidato.setPretencaoSalarial(Double.parseDouble(campos[9]));

            entrevista.setCandidato(candidato);

            //dados vaga
            Vaga vaga = new Vaga();
            int idVaga = Integer.parseInt(campos[10]);
            vaga.setId(idVaga);
            vaga.setCargo(campos[11]);
            vaga.setSalario(campos[12]);
            vaga.setStatus(campos[13]);
            vaga.setRequisitos(campos[14]);
            vaga.setDepartamento(campos[15]);
            vaga.setDataVaga(campos[16] != null && !campos[16].isEmpty() ? LocalDate.parse(campos[16]) : null);

            entrevista.setVaga(vaga);

            Usuario recrutador = new Recrutador.RecrutadorBuilder().build();
            int idPessoa = Integer.parseInt(campos[17]);
            recrutador.setId(idPessoa);
            recrutador.setNome(campos[18]);
            recrutador.setCpf(campos[19]);
            recrutador.setPerfil(Perfil.valueOf(campos[20]));
            return entrevista;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            return null;
        }
    }

    public void excluirEntrevista(Entrevista entrevistaParaExcluir) throws IOException {
        if (entrevistaParaExcluir == null) {
            return;
        }
        boolean removido = this.entrevistaEmMemoria.removeIf(entrevista -> entrevista.getId() == entrevistaParaExcluir.getId());
        if (removido) {
            persistirAlteracoesNoCSV();
        }
    }

    public void atualizarEntrevista() throws IOException {
        persistirAlteracoesNoCSV();
    }
}
