package app.humanize.repository;

import app.humanize.model.Relatorio;
import app.humanize.model.TipoRelatorio;
import app.humanize.model.Usuario;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RelatorioRepository extends BaseRepository {
    private static final RelatorioRepository instance = new RelatorioRepository();
    private static final String NOME_ARQUIVO = "relatorios.csv";
    private final List<Relatorio> relatoriosEmMemoria;

    private RelatorioRepository() {
        this.relatoriosEmMemoria = new ArrayList<>();
        this.carregarRelatoriosDoCSV();
    }

    public static RelatorioRepository getInstance() {
        return instance;
    }

    public List<Relatorio> getTodosRelatorios() {
        return new ArrayList<>(this.relatoriosEmMemoria);
    }

    public int getProximoId() {
        return this.relatoriosEmMemoria.stream()
                .mapToInt(Relatorio::getId)
                .max()
                .orElse(0)
                + 1;
    }

    public void escreverRelatorioNovo(Relatorio relatorio) throws IOException {
        this.relatoriosEmMemoria.add(relatorio);
        this.persistirAlteracoesNoCSV();
    }

    private void carregarRelatoriosDoCSV() {
        File file = getArquivoDePersistencia(NOME_ARQUIVO);
        if (!file.exists()) {
            System.out.println("Arquivo " + NOME_ARQUIVO + " não encontrado. Copiando arquivo padrão...");
            try {
                copiarArquivoDefaultDeResources(NOME_ARQUIVO, file);
            } catch (IOException e) {
                System.err.println("!!! FALHA CRÍTICA AO COPIAR ARQUIVO PADRÃO: " + NOME_ARQUIVO);
                e.printStackTrace();
                return;
            }
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(file))){
            String header = leitor.readLine();
            if (header == null || !header.equals("ID;TipoRelatorio;DataGeracao;ResponsavelID")) {
                System.err.println("Cabeçalho inválido no arquivo CSV de relatórios.");
            }
            String linha;
            while ((linha = leitor.readLine()) != null) {
                Relatorio relatorio = parseRelatorioDaLinhaCsv(linha);
                if (relatorio != null) {
                    this.relatoriosEmMemoria.add(relatorio);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar relatórios do arquivo CSV: " + e.getMessage());
        }
    }

    private Relatorio parseRelatorioDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length != 4) {
            System.err.println("Erro CSV Relatorio: Número incorreto de campos (esperado 4) -> " + linha);
            return null;
        }
        try {
            int id = Integer.parseInt(campos[0].trim());
            TipoRelatorio tipoRelatorio = TipoRelatorio.valueOf(campos[1].trim().toUpperCase());
            LocalDate dataDeGeracao = LocalDate.parse(campos[2].trim());
            int responsavelId = Integer.parseInt(campos[3].trim());

            UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
            Optional<Usuario> responsavelOpt = usuarioRepository.buscaUsuarioPorId(responsavelId);

            if (responsavelOpt.isEmpty()) {
                System.err.println("Erro Parse Relatorio: Responsável ID " + responsavelId + " não encontrado -> " + linha);
                return null;
            }

            Relatorio relatorio = new Relatorio();
            relatorio.setId(id);
            relatorio.setTipoRelatorio(tipoRelatorio);
            relatorio.setDataGeracao(dataDeGeracao);
            relatorio.setResponsavel(responsavelOpt.get());

            return relatorio;
        } catch (IllegalArgumentException e) {
            System.err.println("Erro Parse Relatorio: Tipo inválido ('" + campos[1].trim() + "') -> " + linha + ". Erro: " + e.getMessage());
            return null;
        } catch (DateTimeParseException e) {
            System.err.println("Erro Parse Relatorio: Data inválida ('" + campos[2].trim() + "') -> " + linha + ". Erro: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Erro inesperado Parse Relatorio -> " + linha + ". Erro: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void persistirAlteracoesNoCSV() throws IOException {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        try (FileWriter escritor = new FileWriter(arquivo, false)) {
            escritor.write("ID;TipoRelatorio;DataGeracao;ResponsavelID\n");
            for (Relatorio relatorio : this.relatoriosEmMemoria) {
                escritor.write(formatarRelatorioParaCSV(relatorio));
            }
        }
    }

    private String formatarRelatorioParaCSV(Relatorio relatorio) {
        StringBuilder sb = new StringBuilder();
        sb.append(relatorio.getId()).append(";");
        sb.append(relatorio.getTipoRelatorio().name()).append(";");
        sb.append(relatorio.getDataGeracao().toString()).append(";");
        sb.append(relatorio.getResponsavel() != null ? relatorio.getResponsavel().getId() : "").append("\n");
        return sb.toString();
    }

    public void excluirRelatorio (Relatorio relatorioParaExcluir) throws IOException {
        if (relatorioParaExcluir == null) {
            System.err.println("Tentativa de excluir relatório nulo.");
            return;
        }
        boolean removido = this.relatoriosEmMemoria.removeIf(relatorio -> relatorio.getId() == relatorioParaExcluir.getId());
        if (removido) {
            persistirAlteracoesNoCSV();
            System.out.println("Registro de relatório ID " + relatorioParaExcluir.getId() + " excluído.");
        } else {
            System.err.println("Relatório ID " + relatorioParaExcluir.getId() + " não encontrado para exclusão.");
        }
    }

}