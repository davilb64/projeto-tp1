package app.humanize.service.formatters;

import app.humanize.service.relatorios.ReportData;

public interface IReportFormatter {

    /**
     * Converte os dados brutos em um arquivo (byte array).
     */
    byte[] formatar(ReportData data);

    /**
     * Retorna a extensão do arquivo
     */
    String getExtensao();

    /**
     * Retorna a descrição para o FileChooser
     */
    String getDescricaoFiltro();
}