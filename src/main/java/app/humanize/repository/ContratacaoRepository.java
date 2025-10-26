package app.humanize.repository;

import app.humanize.model.Contratacao;
import app.humanize.model.Vaga;

import java.util.ArrayList;
import java.util.List;

public class ContratacaoRepository {

    private static final ContratacaoRepository instance = new ContratacaoRepository();
    private final String arquivoCsv = "./src/main/resources/contratacoes.csv";
    private final List<Contratacao> contratacoesEmMemoria;

    private ContratacaoRepository() {
        this.contratacoesEmMemoria = new ArrayList<>();
    }

    public static ContratacaoRepository getInstance() {
        return instance;
    }
}
