package app.humanize.service.relatorios;

import app.humanize.model.ContraCheque; // Importe o modelo correto que seu repositório usa
import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.ContrachequeRepository; // Use o repositório fornecido
import app.humanize.util.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class RelatorioContracheque implements IGeradorRelatorio {

    private final ContrachequeRepository contraChequeRepo = ContrachequeRepository.getInstance();
    private final ResourceBundle bundle = UserSession.getInstance().getBundle();
    // Use o formato de data do seu modelo (LocalDate)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Reutiliza a chave de bundle que já definimos para formato de moeda
    private final String currencyFormat = bundle.getString("financialReport.currencyFormat");

    @Override
    public String getNome() {
        return bundle.getString("report.name.payslip"); // "Contracheque"
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

        // 1. Carrega os contracheques do repositório
        List<ContraCheque> cheques = contraChequeRepo.carregarContraChequesPorFuncionario(usuarioLogado.getNome());

        if (cheques.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noPayslipFound"));
        }

        // 2. Encontra o contracheque mais recente
        Optional<ContraCheque> optCheque = cheques.stream()
                .sorted(Comparator.comparing(ContraCheque::getDataEmissao).reversed())
                .findFirst();

        if (optCheque.isEmpty()) {
            return ReportData.empty(bundle.getString("report.error.noPayslipFound"));
        }

        ContraCheque cheque = optCheque.get();
        String dataFormatada = cheque.getDataEmissao().format(dateFormatter);
        String titulo = String.format(bundle.getString("report.payslip.title"), dataFormatada);

        // 3. Define os cabeçalhos (Item e Valor)
        List<String> headers = List.of(
                bundle.getString("report.payslip.header.field"),
                bundle.getString("report.payslip.header.value")
        );

        // 4. Monta as linhas do relatório
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(bundle.getString("report.payslip.field.name"), cheque.getNomeFuncionario()));
        rows.add(List.of(bundle.getString("report.payslip.field.date"), dataFormatada));
        rows.add(List.of("---", "---")); // Separador visual
        rows.add(List.of(bundle.getString("payslip.detail.additions"), String.format(currencyFormat, cheque.getTotalProventos())));
        rows.add(List.of(bundle.getString("payslip.detail.deductions"), String.format(currencyFormat, cheque.getTotalDescontos())));
        rows.add(List.of("---", "---"));
        rows.add(List.of(bundle.getString("report.payslip.field.netTotal"), String.format(currencyFormat, cheque.getSaldo())));

        return new ReportData(titulo, headers, rows);
    }
}