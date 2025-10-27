package app.humanize.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalarioRepository {

    private static final SalarioRepository instance = new SalarioRepository();
    private final String arquivoCsv = "./src/main/resources/regras_salariais.csv";

    private final Map<String, String> regrasEmMemoria;

    private SalarioRepository() {
        this.regrasEmMemoria = new HashMap<>();
        this.carregarRegrasDoCSV();
    }

    public static SalarioRepository getInstance() {
        return instance;
    }

    public Map<String, String> getTodasRegras() {
        return new HashMap<>(this.regrasEmMemoria);
    }

    public void salvarRegra(String chave, String valor) throws IOException {
        this.regrasEmMemoria.put(chave, valor);
        this.persistirAlteracoesNoCSV();
    }

    public String buscarRegra(String chave) {
        return this.regrasEmMemoria.get(chave);
    }

    public boolean existeRegra(String chave) {
        return this.regrasEmMemoria.containsKey(chave);
    }

    public void excluirRegra(String chave) throws IOException {
        if (this.regrasEmMemoria.remove(chave) != null) {
            persistirAlteracoesNoCSV();
        }
    }

    public void carregarRegrasDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) {
            return;
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 2) {
                    String chave = campos[0];
                    String valor = campos[1];
                    this.regrasEmMemoria.put(chave, valor);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar regras do arquivo CSV: " + e.getMessage());
        }
    }

    private void persistirAlteracoesNoCSV() throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("Chave;Valor\n");
            for (Map.Entry<String, String> entry : this.regrasEmMemoria.entrySet()) {
                escritor.write(entry.getKey() + ";" + entry.getValue() + ";\n");
            }
        }
    }

    public List<String> getTodosCargos() {
        List<String> cargos = new ArrayList<>();
        for (String chave : regrasEmMemoria.keySet()) {
            String[] partes = chave.split("_");
            if (partes.length > 0) {
                cargos.add(partes[0]);
            }
        }
        return cargos;
    }

    public int getQuantidadeRegras() {
        return this.regrasEmMemoria.size();
    }
}