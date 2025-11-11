package app.humanize.service.relatorios;

import app.humanize.model.Perfil;
import app.humanize.model.RelatorioFinanceiro;
import app.humanize.model.Usuario;
import app.humanize.repository.RelatorioFinanceiroRepository;
import app.humanize.util.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Gera um relatório financeiro geral (extrato de fluxo de caixa)
 * para a empresa.
 * Acesso: ADMINISTRADOR
 */
public class RelatorioFinanceiroGeral implements IGeradorRelatorio {

    private final RelatorioFinanceiroRepository relatorioRepo = RelatorioFinanceiroRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");
    // Símbolo de moeda para limpeza (ex: "R$")
    private final String currencySymbol = bundle.getString("financialReport.currencySymbol");

    @Override
    public String getNome() {
        // CHAVE NOVA: "Relatório Financeiro Geral"
        return bundle.getString("report.name.financialGeneral");
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
    }

    @Override
    public ReportData coletarDados() {
        // CHAVE NOVA: "Relatório Financeiro Geral (Fluxo de Caixa)"
        String titulo = bundle.getString("report.financialGeneral.title");

        // 1. Cabeçalhos (baseado no seu RelatorioFinanceiroController)
        List<String> headers = List.of(
                bundle.getString("financialReport.column.date"),       // "Data"
                bundle.getString("financialReport.column.description"), // "Descrição"
                bundle.getString("financialReport.column.category"),    // "Categoria"
                bundle.getString("financialReport.column.revenue"),     // "Receita"
                bundle.getString("financialReport.column.expense")      // "Despesa"
        );

        // 2. Carrega todas as transações
        // (Assume que seu repo já inclui os pagamentos de folha, como o controller fazia)
        List<RelatorioFinanceiro> transacoes = relatorioRepo.carregarTransacoes();

        if (transacoes.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noHistoryFound"));
        }

        // 3. Processa linhas e calcula totais
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

            // Acumula totais
            totalReceita += extrairValorNumerico(transacao.getReceita());
            totalDespesa += extrairValorNumerico(transacao.getDespesas());
        }

        double saldoFinal = totalReceita - totalDespesa;

        // 4. Adiciona linhas de resumo
        rows.add(List.of("---", "---", "---", "---", "---"));
        // CHAVE NOVA: "Total de Receitas"
        rows.add(List.of(bundle.getString("report.financialGeneral.totalRevenue"), "", "", String.format(currencyFormat, totalReceita), ""));
        // CHAVE NOVA: "Total de Despesas"
        rows.add(List.of(bundle.getString("report.financialGeneral.totalExpense"), "", "", "", String.format(currencyFormat, totalDespesa)));
        rows.add(List.of("---", "---", "---", "---", "---"));
        // CHAVE NOVA: "Saldo Final"
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
            // Remove o símbolo, espaços, e troca vírgula por ponto
            String valorLimpo = valorComRS.replace(currencySymbol, "")
                    .replace(".", "") // Remove separador de milhar
                    .replace(",", ".") // Troca separador decimal
                    .trim();
            return Double.parseDouble(valorLimpo);
        } catch (NumberFormatException e) {
            System.err.println("Falha ao parsear valor: " + valorComRS);
            return 0.0;
        }
    }
}