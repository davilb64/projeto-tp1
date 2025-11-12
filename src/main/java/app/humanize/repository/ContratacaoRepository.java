package app.humanize.repository;

import app.humanize.model.Candidato;
import app.humanize.model.Contratacao;
import app.humanize.model.Vaga;
import app.humanize.model.StatusVaga;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratacaoRepository extends BaseRepository {

    private static final ContratacaoRepository instance = new ContratacaoRepository();
    private static final String NOME_ARQUIVO = "contratacoes.csv";
    private final List<Contratacao> contratacoesEmMemoria;

    private ContratacaoRepository() {
        this.contratacoesEmMemoria = new ArrayList<>();
        this.carregarContratacaoDoCSV();
    }

    public static ContratacaoRepository getInstance() {
        return instance;
    }

    //metodos de salvar no csv
    public void escreveContracaoNova(Contratacao contratacao) throws IOException {
        int proximoId = getProximoId();
        contratacao.setId(proximoId);

        this.contratacoesEmMemoria.add(contratacao);
        this.persistirAlteracoesNoCSV();
    }
    public int getProximoId() {
        return this.contratacoesEmMemoria.stream()
                .mapToInt(Contratacao::getId)
                .max()
                .orElse(0)
                + 1;
    }
    private void persistirAlteracoesNoCSV() throws IOException {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        try (FileWriter escritor = new FileWriter(arquivo, false)) {
            escritor.write("idContratacao;DataContratacao;RegimeContratacao;Nome;CPF;Email;Telefone;Formacao;Disponibilidade;Pretencao;idVaga;Cargo;Salario;Status;Requisitos;Departamento;DataVaga;\n");
            for (Contratacao contratacao : this.contratacoesEmMemoria) {
                escritor.write(formatarContratacaoParaCSV(contratacao));
            }
        }
    }

    private String formatarContratacaoParaCSV(Contratacao contratacao) {
        StringBuilder sb = new StringBuilder();
        sb.append(contratacao.getId()).append(";");
        sb.append(contratacao.getDataContratacao() == null ? "" : contratacao.getDataContratacao()).append(";");
        sb.append(contratacao.getRegime() == null ? "" : contratacao.getRegime()).append(";");

        if(contratacao.getCandidato() != null){
            sb.append(contratacao.getCandidato().getNome() == null ? "" : contratacao.getCandidato().getNome()).append(";");
            sb.append(contratacao.getCandidato().getCpf() == null ? "" : contratacao.getCandidato().getCpf()).append(";");
            sb.append(contratacao.getCandidato().getEmail() == null ? "" : contratacao.getCandidato().getEmail()).append(";");
            sb.append(contratacao.getCandidato().getTelefone() == null ? "" : contratacao.getCandidato().getTelefone()).append(";");
            sb.append(contratacao.getCandidato().getFormacao() == null ? "" : contratacao.getCandidato().getFormacao()).append(";");
            sb.append(contratacao.getCandidato().getDisponibilidade() == null ? "" : contratacao.getCandidato().getDisponibilidade()).append(";");
            sb.append(contratacao.getCandidato().getPretencaoSalarial()).append(";");
        }
        if(contratacao.getVaga() != null){
            sb.append(contratacao.getVaga().getId()).append(";");
            sb.append(contratacao.getVaga().getCargo() == null ? "" : contratacao.getVaga().getCargo()).append(";");
            sb.append(contratacao.getVaga().getSalario() == null ? "" : contratacao.getVaga().getSalario()).append(";");
            sb.append(contratacao.getVaga().getStatus() == null ? "" : contratacao.getVaga().getStatus()).append(";");
            sb.append(contratacao.getVaga().getRequisitos() == null ? "" : contratacao.getVaga().getRequisitos()).append(";");
            sb.append(contratacao.getVaga().getDepartamento() == null ? "" : contratacao.getVaga().getDepartamento()).append(";");
            sb.append(contratacao.getVaga().getDataVaga() == null ? "" : contratacao.getVaga().getDataVaga()).append(";");
        }
        sb.append("\n");
        return sb.toString();
    }

    //recuperar dados do arquivo csv
    public void carregarContratacaoDoCSV() {
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
            leitor.readLine();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                Contratacao contratacao = parseContratacaoDaLinhaCsv(linha);
                if (contratacao != null) {
                    this.contratacoesEmMemoria.add(contratacao);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar vagas do arquivo CSV: " + e.getMessage());
        }
    }

    private Contratacao parseContratacaoDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 16) return null;

        try {
            int idContratacao = Integer.parseInt(campos[0]);

            Contratacao contratacao = new Contratacao();
            contratacao.setId(idContratacao);
            contratacao.setDataContratacao(campos[1] != null && !campos[1].isEmpty() ? LocalDate.parse(campos[1]) : null);
            contratacao.setRegime(campos[2]);

            //dados do candidato
            Candidato candidato = new Candidato();
            candidato.setNome(campos[3]);
            candidato.setCpf(campos[4]);
            candidato.setEmail(campos[5]);
            candidato.setTelefone(campos[6]);
            candidato.setFormacao(campos[7]);
            candidato.setDisponibilidade(campos[8]);
            candidato.setPretencaoSalarial(Double.parseDouble(campos[9]));

            contratacao.setCandidato(candidato);

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

            contratacao.setVaga(vaga);

            return contratacao;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            return null;
        }
    }

}