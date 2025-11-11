package app.humanize.service.relatorios;

import app.humanize.model.ContraCheque; // Importe o modelo correto que seu repositório usa
import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.ContrachequeRepository; // Use o repositório fornecido
import app.humanize.util.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RelatorioHistoricoFinanceiro implements IGeradorRelatorio {

    private final ContrachequeRepository contraChequeRepo = ContrachequeRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");

    @Override
    public String getNome() {
        return bundle.getString("report.name.financialHistory"); // "Histórico Financeiro"
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return true;
    }

    @Override
    public ReportData coletarDados() {
        Usuario usuarioLogado = UserSession.getInstance().getUsuarioLogado();
        if (usuarioLogado == null) {
            return ReportData.empty(bundle.getString("report.error.notLoggedIn"));
        }

        String titulo = String.format(bundle.getString("report.financialHistory.title"), usuarioLogado.getNome());

        // 1. Define os cabeçalhos da tabela
        List<String> headers = List.of(
                bundle.getString("report.financialHistory.header.date"),
                bundle.getString("report.financialHistory.header.gross"), // (Proventos)
                bundle.getString("report.financialHistory.header.deductions"), // (Descontos)
                bundle.getString("report.financialHistory.header.net") // (Líquido)
        );

        // 2. Carrega e ordena os contracheques por data
        List<ContraCheque> cheques = contraChequeRepo.carregarContraChequesPorFuncionario(usuarioLogado.getNome())
                .stream()
                .sorted(Comparator.comparing(ContraCheque::getDataEmissao))
                .collect(Collectors.toList());

        if (cheques.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noHistoryFound"));
        }

        // 3. Mapeia os dados para as linhas do relatório
        List<List<String>> rows = new ArrayList<>();
        for (ContraCheque cheque : cheques) {
            rows.add(List.of(
                    cheque.getDataEmissao().format(dateFormatter),
                    String.format(currencyFormat, cheque.getTotalProventos()),
                    String.format(currencyFormat, cheque.getTotalDescontos()),
                    String.format(currencyFormat, cheque.getSaldo())
            ));
        }

        return new ReportData(titulo, headers, rows);
    }
}