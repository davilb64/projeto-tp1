package app.humanize.repository;

import app.humanize.model.factories.RelatorioFinanceiro;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RelatorioFinanceiroRepository {

    public static final RelatorioFinanceiroRepository instance = new RelatorioFinanceiroRepository();
    private final String arquivoCsv = "./src/main/resources/relatorio_financeiro.csv";

    private RelatorioFinanceiroRepository() {}

    public static RelatorioFinanceiroRepository getInstance() {
        return instance;
    }

    public void criarArquivoSeNaoExiste() throws IOException {
        File arquivo = new File(arquivoCsv);
        System.out.println("🔍 Verificando se arquivo existe: " + arquivo.getAbsolutePath());
        if (!arquivo.exists()) {
            System.out.println("📁 Arquivo NÃO existe - criando...");
            try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
                escritor.write("Data;Descricao;Receita;Despesas;Valor;Saldo;Categoria\n");
            }
            System.out.println("✅ Arquivo criado com sucesso!");
        } else {
            System.out.println("✅ Arquivo JÁ existe!");
        }
    }

    public void salvarTransacoes(List<RelatorioFinanceiro> transacoes) throws IOException {
        System.out.println("💾 Tentando salvar " + transacoes.size() + " transações...");

        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("Data;Descricao;Receita;Despesas;Valor;Saldo;Categoria\n");

            for (RelatorioFinanceiro transacao : transacoes) {
                escritor.write(String.format("%s;%s;%s;%s;%s;%s;%s\n",
                        transacao.getData(),
                        transacao.getDescricao(),
                        transacao.getReceita(),
                        transacao.getDespesas(),
                        transacao.getValor(),
                        transacao.getSaldo(),
                        transacao.getCategoria()));
            }
        }
        System.out.println("✅ Transações salvas com sucesso!");
    }

    public List<RelatorioFinanceiro> carregarTransacoes() {
        System.out.println("📥 Tentando carregar transações...");
        List<RelatorioFinanceiro> transacoes = new ArrayList<>();
        File arquivo = new File(arquivoCsv);

        if (!arquivo.exists()) {
            System.out.println("❌ Arquivo NÃO existe - retornando lista vazia");
            return transacoes;
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();
            String linha;
            int count = 0;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 7) {
                    RelatorioFinanceiro transacao = new RelatorioFinanceiro(
                            campos[0], campos[1], campos[2], campos[3],
                            campos[4], campos[5], campos[6]
                    );
                    transacoes.add(transacao);
                    count++;
                }
            }
            System.out.println("✅ Carregadas " + count + " transações do arquivo");
        } catch (IOException e) {
            System.out.println("❌ Erro ao carregar: " + e.getMessage());
        }

        return transacoes;
    }
}