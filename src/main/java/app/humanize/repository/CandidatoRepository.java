package app.humanize.repository;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CandidatoRepository {

    private static final CandidatoRepository instance = new CandidatoRepository();
    private final String arquivoCsv = "./src/main/resources/candidatos.csv";
    private final List<Candidato> candidatosEmMemoria = new ArrayList<>();

    private CandidatoRepository() {
        carregarDoCSV();
    }

    public static CandidatoRepository getInstance() {
        return instance;
    }

    public List<Candidato> getTodos() {
        return new ArrayList<>(candidatosEmMemoria);
    }

    public int getQtdCandidatos() {
        return candidatosEmMemoria.size();
    }

    public void adicionar(Candidato candidato) throws IOException {
        candidatosEmMemoria.add(candidato);
        persistirNoCSV();
    }

    public void remover(Candidato candidato) throws IOException {
        candidatosEmMemoria.removeIf(c -> c.getCpf().equals(candidato.getCpf()));
        persistirNoCSV();
    }

    public void atualizar() throws IOException {
        persistirNoCSV();
    }

    private void carregarDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            reader.readLine(); // cabeçalho
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 8) {
                    Vaga vaga = new Vaga();
                    vaga.setCargo(campos[7]);

                    Candidato c = new Candidato.CandidatoBuilder()
                            .nome(campos[0])
                            .cpf(campos[1])
                            .email(campos[2])
                            .telefone(campos[3])
                            .formacao(campos[4])
                            .disponibilidade(campos[5])
                            .pretencaoSalarial(Double.parseDouble(campos[6]))
                            .experiencia(campos[7])
                            .dataCadastro(LocalDate.now())
                            .build();

                    candidatosEmMemoria.add(c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void persistirNoCSV() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoCsv))) {
            writer.write("Nome;CPF;Email;Telefone;Formacao;Disponibilidade;Pretencao;Vaga;DataCadastro;Documento\n");
            for (Candidato c : candidatosEmMemoria) {
                writer.write(formatarCandidato(c));
                writer.newLine();
            }
        }
    }

    private String formatarCandidato(Candidato c) {
        return String.join(";",
                c.getNome(),
                c.getCpf(),
                c.getEmail(),
                c.getTelefone(),
                c.getFormacao(),
                c.getExperiencia(),
                c.getDisponibilidade(),
                String.valueOf(c.getPretencaoSalarial()),
                c.getCaminhoDocumento() != null ? c.getCaminhoDocumento() : ""

        );
    }
}
