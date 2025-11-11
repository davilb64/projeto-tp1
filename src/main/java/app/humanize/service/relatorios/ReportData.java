package app.humanize.service.relatorios;

import java.util.Collections;
import java.util.List;

public class ReportData {
    private final String title;
    private final List<String> headers;
    private final List<List<String>> rows;

    public ReportData(String title, List<String> headers, List<List<String>> rows) {
        this.title = title != null ? title : "Relatório";
        this.headers = headers;
        this.rows = rows;
    }

    public String getTitle() { return title; }
    public List<String> getHeaders() { return headers; }
    public List<List<String>> getRows() { return rows; }

    /**
     * NOVO: Método helper para criar um relatório de status/erro.
     */
    public static ReportData empty(String message) {
        return new ReportData(
                "Aviso de Relatório",
                List.of("Status"),
                List.of(List.of(message != null ? message : "Nenhum dado encontrado"))
        );
    }
}