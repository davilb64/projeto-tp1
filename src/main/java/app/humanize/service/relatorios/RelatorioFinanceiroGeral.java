package app.humanize.service.relatorios;

import app.humanize.model.Perfil;
import app.humanize.model.RelatorioFinanceiro;
import app.humanize.model.Usuario;
import app.humanize.repository.RelatorioFinanceiroRepository;
import app.humanize.util.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RelatorioFinanceiroGeral implements IGeradorRelatorio {

    private final RelatorioFinanceiroRepository relatorioRepo = RelatorioFinanceiroRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");
    private final String currencySymbol = bundle.getString("financialReport.currencySymbol");

    @Override
    public String getNome() {
        return bundle.getString("report.name.financialGeneral");
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
    }

    @Override
    public ReportData coletarDados() {
        String titulo = bundle.getString("report.financialGeneral.title");

        List<String> headers = List.of(
                bundle.getString("financialReport.column.date"),
                bundle.getString("financialReport.column.description"),
                bundle.getString("financialReport.column.category"),
                bundle.getString("financialReport.column.revenue"),
                bundle.getString("financialReport.column.expense")
        );

        List<RelatorioFinanceiro> transacoes = relatorioRepo.carregarTransacoes();

        if (transacoes.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noHistoryFound"));
        }

        List<List<String>> rows = new ArrayList<>();
        double totalReceita = 0.0;
        double totalDespesa = 0.0;

        for (RelatorioFinanceiro transacao : transacoes) {
            rows.add(List.of(
                    transacao.getData(),
                    transacao.getDescricao(),
                    transacao.getCategoria(),
                    transacao.getReceita(),
                    transacao.getDespesas()
            ));

            totalReceita += extrairValorNumerico(transacao.getReceita());
            totalDespesa += extrairValorNumerico(transacao.getDespesas());
        }

        double saldoFinal = totalReceita - totalDespesa;

        rows.add(List.of("---", "---", "---", "---", "---"));
        rows.add(List.of(bundle.getString("report.financialGeneral.totalRevenue"), "", "", String.format(currencyFormat, totalReceita), ""));
        rows.add(List.of(bundle.getString("report.financialGeneral.totalExpense"), "", "", "", String.format(currencyFormat, totalDespesa)));
        rows.add(List.of("---", "---", "---", "---", "---"));
        rows.add(List.of(bundle.getString("report.financialGeneral.finalBalance"), "", "", "", String.format(currencyFormat, saldoFinal)));

        return new ReportData(titulo, headers, rows);
    }

    /**
     * Helper para converter o valor em string (ex: "R$ 1.500,00") para double.
     * Copiado do seu RelatorioFinanceiroController.
     */
    private double extrairValorNumerico(String valorComRS) {
        if (valorComRS == null || valorComRS.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String valorLimpo = valorComRS.replace(currencySymbol, "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();
            return Double.parseDouble(valorLimpo);
        } catch (NumberFormatException e) {
            System.err.println("Falha ao parsear valor: " + valorComRS);
            return 0.0;
        }
    }
}