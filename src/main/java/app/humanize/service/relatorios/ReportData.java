package app.humanize.service.relatorios;

import java.util.List;

public class ReportData {
    private final String title; // <-- NOVO CAMPO
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
}