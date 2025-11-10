package app.humanize.repository;

import app.humanize.model.RelatorioFinanceiro;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RelatorioFinanceiroRepository extends BaseRepository {

    public static final RelatorioFinanceiroRepository instance = new RelatorioFinanceiroRepository();
    private static final String NOME_ARQUIVO = "relatorio_financeiro.csv";

    private RelatorioFinanceiroRepository() {}

    public static RelatorioFinanceiroRepository getInstance() {
        return instance;
    }

    public void criarArquivoSeNaoExiste() throws IOException {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        if (!arquivo.exists()) {
            try (FileWriter escritor = new FileWriter(arquivo, false)) {
                escritor.write("Data;Descricao;Receita;Despesas;Saldo;Categoria\n");
            }
        }
    }

    public void salvarTransacoes(List<RelatorioFinanceiro> transacoes) throws IOException {
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);
        try (FileWriter escritor = new FileWriter(arquivo, false)) {
            escritor.write("Data;Descricao;Receita;Despesas;Saldo;Categoria\n");

            for (RelatorioFinanceiro transacao : transacoes) {
                escritor.write(String.format("%s;%s;%s;%s;%s;%s\n",
                        transacao.getData(),
                        transacao.getDescricao(),
                        transacao.getReceita(),
                        transacao.getDespesas(),
                        transacao.getSaldo(),
                        transacao.getCategoria()));
            }
        }
    }

    public List<RelatorioFinanceiro> carregarTransacoes() {
        List<RelatorioFinanceiro> transacoes = new ArrayList<>();
        File arquivo = getArquivoDePersistencia(NOME_ARQUIVO);

        if (!arquivo.exists()) {
            System.out.println("Arquivo " + NOME_ARQUIVO + " não encontrado. Copiando arquivo padrão...");
            try {
                copiarArquivoDefaultDeResources(NOME_ARQUIVO, arquivo);
            } catch (IOException e) {
                System.err.println("!!! FALHA CRÍTICA AO COPIAR ARQUIVO PADRÃO: " + NOME_ARQUIVO);
                e.printStackTrace();
                return transacoes;
            }
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 6) {
                    RelatorioFinanceiro transacao = new RelatorioFinanceiro(
                            campos[0],  // data
                            campos[1],  // descricao
                            campos[2],  // receita
                            campos[3],  // despesas
                            campos[4],  // saldo
                            campos[5]   // categoria
                    );
                    transacoes.add(transacao);
                }
            }
        } catch (IOException e) {
        }

        return transacoes;
    }
}