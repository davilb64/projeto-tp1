package app.humanize.repository;

import app.humanize.model.ContraCheque;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContrachequeRepository {

    private static final ContrachequeRepository instance = new ContrachequeRepository();
    private final String arquivoCsv = "./src/main/resources/contracheques.csv";

    private ContrachequeRepository() {}

    public static ContrachequeRepository getInstance() {
        return instance;
    }

    public List<ContraCheque> carregarContraChequesPorFuncionario(String nomeFuncionario) {
        List<ContraCheque> todosContraCheques = carregarTodosContraCheques();

        return todosContraCheques.stream()
                .filter(contraCheque -> contraCheque.getNomeFuncionario().equalsIgnoreCase(nomeFuncionario))
                .collect(Collectors.toList());
    }

    private List<ContraCheque> carregarTodosContraCheques() {
        List<ContraCheque> contraCheques = new ArrayList<>();
        File arquivo = new File(arquivoCsv);

        if (!arquivo.exists()) {
            return contraCheques;
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine(); // Pula header
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 5) {
                    String nomeFuncionario = campos[0];
                    LocalDate dataEmissao = LocalDate.parse(campos[1]);
                    double totalProventos = Double.parseDouble(campos[2]);
                    double totalDescontos = Double.parseDouble(campos[3]);
                    double saldo = Double.parseDouble(campos[4]);

                    ContraCheque contraCheque = new ContraCheque(nomeFuncionario, dataEmissao, totalProventos, totalDescontos, saldo);
                    contraCheques.add(contraCheque);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro ao carregar contracheques do CSV: " + e.getMessage());
        }

        return contraCheques;
    }

    public void salvarContraCheque(ContraCheque contraCheque) throws IOException {
        List<ContraCheque> contraChequesExistentes = carregarTodosContraCheques();
        contraChequesExistentes.add(contraCheque);
        salvarTodosContraCheques(contraChequesExistentes);
    }

    private void salvarTodosContraCheques(List<ContraCheque> contraCheques) throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("NomeFuncionario;DataEmissao;TotalProventos;TotalDescontos;Saldo\n");

            for (ContraCheque contraCheque : contraCheques) {
                escritor.write(String.format("%s;%s;%.2f;%.2f;%.2f\n",
                        contraCheque.getNomeFuncionario(),
                        contraCheque.getDataEmissao(),
                        contraCheque.getTotalProventos(),
                        contraCheque.getTotalDescontos(),
                        contraCheque.getSaldo()));
            }
        }
    }
}