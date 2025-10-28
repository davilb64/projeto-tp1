package app.humanize.service.relatorios;

import app.humanize.model.Usuario;

public interface IGeradorRelatorio {
    String getNome();

    ReportData coletarDados();

    /**
     * Verifica se o usuário logado tem permissão para gerar este relatório.
     * @param usuarioLogado O usuário da sessão.
     * @return true se ele tiver permissão, false caso contrário.
     */
    boolean podeGerar(Usuario usuarioLogado);
}