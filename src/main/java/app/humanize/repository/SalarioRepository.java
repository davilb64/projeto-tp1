package app.humanize.repository;

import app.humanize.model.RegraSalarial;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SalarioRepository {

    private static final SalarioRepository instance = new SalarioRepository();
    private final String arquivoCsv = "./src/main/resources/regras_salariais.csv";

    private SalarioRepository() {}

    public static SalarioRepository getInstance() {
        return instance;
    }

    public void salvarRegra(RegraSalarial regra) throws IOException {
        List<RegraSalarial> regrasExistentes = carregarTodasRegras();

        regrasExistentes.removeIf(existente ->
                existente.getCargo().equals(regra.getCargo()) &&
                        existente.getNivel().equals(regra.getNivel()));

        regrasExistentes.add(regra);
        salvarTodasRegras(regrasExistentes);
    }

    public List<RegraSalarial> carregarTodasRegras() {
        List<RegraSalarial> regras = new ArrayList<>();
        File arquivo = new File(arquivoCsv);

        if (!arquivo.exists()) {
            return regras;
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] campos = linha.split(";", -1);
                if (campos.length >= 6) {
                    String cargo = campos[0];
                    String nivel = campos[1];
                    double salarioBase = Double.parseDouble(campos[2]);
                    double adicionalNivel = Double.parseDouble(campos[3]);
                    double beneficios = Double.parseDouble(campos[4]);
                    double salarioTotal = Double.parseDouble(campos[5]);

                    RegraSalarial regra = new RegraSalarial(cargo, nivel, salarioBase, adicionalNivel, beneficios, salarioTotal);
                    regras.add(regra);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro ao carregar regras do CSV: " + e.getMessage());
        }

        return regras;
    }

    private void salvarTodasRegras(List<RegraSalarial> regras) throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("Cargo;Nivel;SalarioBase;AdicionalNivel;Beneficios;SalarioTotal\n");

            for (RegraSalarial regra : regras) {
                escritor.write(String.format("%s;%s;%.2f;%.2f;%.2f;%.2f\n",
                        regra.getCargo(),
                        regra.getNivel(),
                        regra.getSalarioBase(),
                        regra.getAdicionalNivel(),
                        regra.getBeneficios(),
                        regra.getSalarioTotal()));
            }
        }
    }
}