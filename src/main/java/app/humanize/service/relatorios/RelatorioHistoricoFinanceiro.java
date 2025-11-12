package app.humanize.service.relatorios;

import app.humanize.model.ContraCheque;
import app.humanize.model.Usuario;
import app.humanize.repository.ContrachequeRepository;
import app.humanize.util.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class RelatorioHistoricoFinanceiro implements IGeradorRelatorio {

    private final ContrachequeRepository contraChequeRepo = ContrachequeRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");

    @Override
    public String getNome() {
        return bundle.getString("report.name.financialHistory");
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

        List<String> headers = List.of(
                bundle.getString("report.financialHistory.header.date"),
                bundle.getString("report.financialHistory.header.gross"),
                bundle.getString("report.financialHistory.header.deductions"),
                bundle.getString("report.financialHistory.header.net")
        );

        List<ContraCheque> cheques = contraChequeRepo.carregarContraChequesPorFuncionario(usuarioLogado.getNome())
                .stream()
                .sorted(Comparator.comparing(ContraCheque::getDataEmissao))
                .toList();

        if (cheques.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noHistoryFound"));
        }

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