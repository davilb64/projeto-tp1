package app.humanize.service.relatorios;

import app.humanize.model.ContraCheque;
import app.humanize.model.Usuario;
import app.humanize.repository.ContrachequeRepository;
import app.humanize.util.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class RelatorioContracheque implements IGeradorRelatorio {

    private final ContrachequeRepository contraChequeRepo = ContrachequeRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");

    @Override
    public String getNome() {
        return bundle.getString("report.name.payslip");
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

        List<ContraCheque> cheques = contraChequeRepo.carregarContraChequesPorFuncionario(usuarioLogado.getNome());

        if (cheques.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noPayslipFound"));
        }

        Optional<ContraCheque> optCheque = cheques.stream().max(Comparator.comparing(ContraCheque::getDataEmissao));

        ContraCheque cheque = optCheque.get();
        String dataFormatada = cheque.getDataEmissao().format(dateFormatter);
        String titulo = String.format(bundle.getString("report.payslip.title"), dataFormatada);

        List<String> headers = List.of(
                bundle.getString("report.payslip.header.field"),
                bundle.getString("report.payslip.header.value")
        );

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(bundle.getString("report.payslip.field.name"), cheque.getNomeFuncionario()));
        rows.add(List.of(bundle.getString("report.payslip.field.date"), dataFormatada));
        rows.add(List.of("---", "---"));
        rows.add(List.of(bundle.getString("payslip.detail.additions"), String.format(currencyFormat, cheque.getTotalProventos())));
        rows.add(List.of(bundle.getString("payslip.detail.deductions"), String.format(currencyFormat, cheque.getTotalDescontos())));
        rows.add(List.of("---", "---"));
        rows.add(List.of(bundle.getString("report.payslip.field.netTotal"), String.format(currencyFormat, cheque.getSaldo())));

        return new ReportData(titulo, headers, rows);
    }
}