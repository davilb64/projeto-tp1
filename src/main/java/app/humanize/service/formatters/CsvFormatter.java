package app.humanize.service.formatters;

import app.humanize.service.relatorios.ReportData;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvFormatter implements IReportFormatter {
    @Override
    public byte[] formatar(ReportData data) {
        StringBuilder sb = new StringBuilder();

        // Cabeçalho
        sb.append(String.join(";", data.getHeaders())).append("\n");

        // Linhas
        for (List<String> row : data.getRows()) {
            sb.append(String.join(";", row)).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getExtensao() {
        return ".csv";
    }

    @Override
    public String getDescricaoFiltro() {
        return "Arquivo CSV (*.csv)";
    }
}
