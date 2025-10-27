package app.humanize.model;

public enum TipoRelatorio {
    LISTA_USUARIOS("Lista Completa de Usuários"),
    FOLHA_PAGAMENTO("Folha de Pagamento Mensal"),
    CONTRACHEQUE_INDIVIDUAL("Contracheque Individual"),
    CANDIDATOS_POR_VAGA("Candidatos por Vaga");

    private final String nomeAmigavel;

    TipoRelatorio(String nome) {
        this.nomeAmigavel = nome;
    }

    // Método para obter o nome bonito para exibição na UI
    @Override
    public String toString() {
        return nomeAmigavel;
    }
}