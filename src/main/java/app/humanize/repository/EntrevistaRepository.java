package app.humanize.repository;

import app.humanize.model.*;
import com.sun.javafx.scene.EnteredExitedHandler;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntrevistaRepository extends BaseRepository {
    private static final EntrevistaRepository instance = new EntrevistaRepository();
    private static final String NOME_ARQUIVO = "entrevistas.csv";
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

    public List<Entrevista> getEntrevistasHoje(){
        LocalDate  hoje = LocalDate.now();
        List<Entrevista> entrevistas = new ArrayList<>();
        for (Entrevista entrevista : entrevistaEmMemoria) {
            if (entrevista.getDataEntrevista() == hoje) {
                entrevistas.add(entrevista);
            }
        }
        return entrevistas;
    }

    public List<Entrevista> buscarCandidatosAprovados() {
        return this.entrevistaEmMemoria.stream()
                .filter(e -> e.getStatus().equals(StatusEntrevista.Aprovado))
                .toList();
    }

    public String buscarNomeCargoEntrevista(int idEntrevista) {
        return this.entrevistaEmMemoria.stream()
                .filter(e -> e.getId() == idEntrevista)
                .map(e -> e.getVaga().getCargo())
                .findFirst().orElse(null);
    }
    public Entrevista buscarEntrevistaPorNomeCandidato(String nome) {
        return this.entrevistaEmMemoria.stream()
                .filter(e -> e.getCandidatura().getCandidato().getNome().equalsIgnoreCase(nome))
                .findFirst().orElse(null);
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
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        try (FileWriter escritor = new FileWriter(arquivo, false)) {
            escritor.write("idEntrevista;DataEntrevista;StatusEntrevista;Nome;CPF;Email;Telefone;Formacao;Disponibilidade;Pretencao;idVaga;Cargo;Salario;Status;Requisitos;Departamento;DataVaga;IdPessoa;NomePessoa;CpfPessoa;PerfilPessoa;StatusCandidatura;dataCandidatura;\n");
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

        if(entrevista.getCandidatura() != null){
            sb.append(entrevista.getCandidatura().getCandidato().getNome() == null ? "" : entrevista.getCandidatura().getCandidato().getNome()).append(";");
            sb.append(entrevista.getCandidatura().getCandidato().getCpf() == null ? "" : entrevista.getCandidatura().getCandidato().getCpf()).append(";");
            sb.append(entrevista.getCandidatura().getCandidato().getEmail() == null ? "" : entrevista.getCandidatura().getCandidato().getEmail()).append(";");
            sb.append(entrevista.getCandidatura().getCandidato().getTelefone() == null ? "" : entrevista.getCandidatura().getCandidato().getTelefone()).append(";");
            sb.append(entrevista.getCandidatura().getCandidato().getFormacao() == null ? "" : entrevista.getCandidatura().getCandidato().getFormacao()).append(";");
            sb.append(entrevista.getCandidatura().getCandidato().getDisponibilidade() == null ? "" : entrevista.getCandidatura().getCandidato().getDisponibilidade()).append(";");
            sb.append(String.valueOf(entrevista.getCandidatura().getCandidato().getPretencaoSalarial())).append(";");
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
        sb.append(entrevista.getCandidatura().getStatus() == null ? "" : entrevista.getCandidatura().getStatus()).append(";");
        sb.append(entrevista.getCandidatura().getDataCandidatura() == null ? "" : entrevista.getCandidatura().getDataCandidatura()).append(";");
        sb.append("\n");
        return sb.toString();
    }

    //recuperar dados do arquivo csv
    public void carregarEntrevistaDoCSV() {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        if (!arquivo.exists()) {
            System.out.println("Arquivo " + NOME_ARQUIVO + " não encontrado. Copiando arquivo padrão...");
            try {
                copiarArquivoDefaultDeResources(NOME_ARQUIVO, arquivo);
            } catch (IOException e) {
                System.err.println("!!! FALHA CRÍTICA AO COPIAR ARQUIVO PADRÃO: " + NOME_ARQUIVO);
                e.printStackTrace();
                return;
            }
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
        if (campos.length < 22) return null;

        try {
            int idContratacao = Integer.parseInt(campos[0]);

            Entrevista entrevista = new Entrevista();
            entrevista.setId(idContratacao);
            entrevista.setDataEntrevista(campos[1] != null && !campos[1].isEmpty() ? LocalDate.parse(campos[1]) : null);
            entrevista.setStatus(campos[2]!= null && !campos[2].isEmpty() ?  StatusEntrevista.valueOf(campos[2]) : null);

            //dados do candidato
            Candidato candidato = new Candidato();
            candidato.setNome(campos[3]);
            candidato.setCpf(campos[4]);
            candidato.setEmail(campos[5]);
            candidato.setTelefone(campos[6]);
            candidato.setFormacao(campos[7]);
            candidato.setDisponibilidade(campos[8]);
            candidato.setPretencaoSalarial(Double.parseDouble(campos[9]));

            //dados vaga
            Vaga vaga = new Vaga();
            int idVaga = Integer.parseInt(campos[10]);
            vaga.setId(idVaga);
            vaga.setCargo(campos[11]);
            vaga.setSalario(campos[12]);
            if (campos[13] != null && !campos[13].isEmpty()) {
                try {
                    vaga.setStatus(StatusVaga.valueOf(campos[13]));
                } catch (IllegalArgumentException e) {
                    System.err.println("Status inválido no CSV: " + campos[13]);
                    vaga.setStatus(StatusVaga.ABERTA); // valor padrão
                }
            }
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

            entrevista.setRecrutador(recrutador);

            Candidatura candidatura = new Candidatura();
            candidatura.setCandidato(candidato);
            candidatura.setVaga(vaga);
            candidatura.setDataCandidatura(campos[21] != null && !campos[21].isEmpty() ? LocalDate.parse(campos[21]) : null);
            candidatura.setStatus(StatusCandidatura.valueOf(campos[22]));
            entrevista.setCandidatura(candidatura);

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