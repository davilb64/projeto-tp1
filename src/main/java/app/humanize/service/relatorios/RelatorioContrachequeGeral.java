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

public class RelatorioContrachequeGeral implements IGeradorRelatorio {

    private final FolhaPagRepository folhaRepo = FolhaPagRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String getNome() {
        return bundle.getString("report.name.payslipGeneral");
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
    }

    @Override
    public ReportData coletarDados() {
        String titulo = bundle.getString("report.payslipGeneral.title");

        List<String> headers = List.of(
                bundle.getString("report.payslip.field.name"),
                bundle.getString("report.payslip.field.date"),
                bundle.getString("report.payslip.field.position"),
                bundle.getString("report.payslip.field.baseSalary"),
                bundle.getString("report.payslip.field.additions"),
                bundle.getString("report.payslip.field.deductions"),
                bundle.getString("report.payslip.field.netTotal")
        );

        List<FolhaPag> todasFolhas = folhaRepo.carregarTodasFolhas();
        if (todasFolhas.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noPayslipFound"));
        }

        todasFolhas.sort(Comparator.comparing(FolhaPag::getNome)
                .thenComparing(FolhaPag::getData));

        List<List<String>> rows = new ArrayList<>();
        double totalBase = 0.0;
        double totalAdicionais = 0.0;
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

            totalBase += folha.getSalarioBase();
            totalAdicionais += proventos;
            totalDescontos += folha.getDescontos();
            totalLiquido += folha.getSalarioLiquido();
        }

        rows.add(List.of("---", "---", "---", "---", "---", "---", "---"));
        rows.add(List.of(
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