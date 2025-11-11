package app.humanize.service.relatorios;

import app.humanize.model.FolhaPag;
import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.FolhaPagRepository;
import app.humanize.util.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Gera um relatório detalhado de todas as folhas de pagamento (contracheques)
 * emitidas para todos os funcionários.
 * Acesso: ADMINISTRADOR
 */
public class RelatorioContrachequeGeral implements IGeradorRelatorio {

    private final FolhaPagRepository folhaRepo = FolhaPagRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");
    // Formato da sua classe FolhaPag (LocalDate.toString() = yyyy-MM-dd)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String getNome() {
        // CHAVE NOVA: "Relatório Geral de Contracheques"
        return bundle.getString("report.name.payslipGeneral");
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
    }

    @Override
    public ReportData coletarDados() {
        // CHAVE NOVA: "Relatório Geral de Folhas de Pagamento"
        String titulo = bundle.getString("report.payslipGeneral.title");

        // 1. Cabeçalhos
        List<String> headers = List.of(
                bundle.getString("report.payslip.field.name"),      // "Nome"
                bundle.getString("report.payslip.field.date"),      // "Data de Referência"
                bundle.getString("report.payslip.field.position"),  // "Cargo"
                bundle.getString("report.payslip.field.baseSalary"),// "Salário Base"
                bundle.getString("report.payslip.field.additions"), // "Adicionais"
                bundle.getString("report.payslip.field.deductions"),// "Descontos"
                bundle.getString("report.payslip.field.netTotal")   // "Salário Líquido"
        );

        // 2. Carrega todos os dados
        List<FolhaPag> todasFolhas = folhaRepo.carregarTodasFolhas();
        if (todasFolhas.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noPayslipFound"));
        }

        // Ordena por nome e depois por data
        todasFolhas.sort(Comparator.comparing(FolhaPag::getNome)
                .thenComparing(FolhaPag::getData));

        // 3. Processa linhas e calcula totais
        List<List<String>> rows = new ArrayList<>();
        double totalBase = 0.0;
        double totalAdicionais = 0.0; // Soma de adicionalNivel, beneficios, adicionais
        double totalDescontos = 0.0;
        double totalLiquido = 0.0;

        for (FolhaPag folha : todasFolhas) {
            double proventos = folha.getAdicionalNivel() + folha.getBeneficios() + folha.getAdicionais();

            rows.add(List.of(
                    folha.getNome(),
                    folha.getData().format(dateFormatter),
                    folha.getCargo(),
                    String.format(currencyFormat, folha.getSalarioBase()),
                    String.format(currencyFormat, proventos),
                    String.format(currencyFormat, folha.getDescontos()),
                    String.format(currencyFormat, folha.getSalarioLiquido())
            ));

            // Acumula totais
            totalBase += folha.getSalarioBase();
            totalAdicionais += proventos;
            totalDescontos += folha.getDescontos();
            totalLiquido += folha.getSalarioLiquido();
        }

        // 4. Adiciona linhas de resumo
        rows.add(List.of("---", "---", "---", "---", "---", "---", "---"));
        rows.add(List.of(
                // CHAVE NOVA: "TOTAIS"
                bundle.getString("report.general.totals"),
                "",
                "",
                String.format(currencyFormat, totalBase),
                String.format(currencyFormat, totalAdicionais),
                String.format(currencyFormat, totalDescontos),
                String.format(currencyFormat, totalLiquido)
        ));

        return new ReportData(titulo, headers, rows);
    }
}