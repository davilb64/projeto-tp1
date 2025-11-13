package app.humanize.model;

/**
 * Enum que representa os tipos de relatórios que podem ser gerados
 * pelo sistema (neste contexto, pela tela de Administrador).
 */
public enum TipoRelatorio {

    /**
     * Mapeia para a classe RelatorioListaUsuarios
     */
    LISTA_USUARIOS,

    /**
     * Mapeia para a classe RelatorioContrachequeGeral
     */
    CONTRACHEQUE_GERAL,

    /**
     * Mapeia para a classe RelatorioFinanceiroGeral
     */
    FINANCEIRO_GERAL
}