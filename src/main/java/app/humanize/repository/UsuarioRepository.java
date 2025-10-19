package app.humanize.repository;

import app.humanize.model.Usuario;
import app.humanize.model.Funcionario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UsuarioRepository {
    private final String arquivoCsv = "./src/main/resources/usuarios.csv";

    private boolean usuarioJaExiste(String idDoUsuario) throws IOException {
        try (FileReader arquivo = new FileReader(arquivoCsv);
             BufferedReader leitura = new BufferedReader(arquivo)) {
            String linha;
            leitura.readLine();
            while ((linha = leitura.readLine()) != null) {
                String[] campos = linha.split(";");
                if (campos.length > 0 && campos[0].equals(idDoUsuario)) {
                    return true;
                }
            }
        } catch (IOException e) {
            if (e instanceof java.io.FileNotFoundException) {
                return false;
            }
            throw e;
        }
        return false;
    }

    public void escreveUsuarioNovo(Usuario usuario) throws IOException {
        String idUsuarioString = String.valueOf(usuario.getId());
        if (usuarioJaExiste(idUsuarioString)) {
            System.out.println("Usuário já cadastrado (ID: " + idUsuarioString + ")\n");
            return;
        }
        File arquivo = new File(arquivoCsv);
        boolean arquivoNaoExiste = !arquivo.exists() || arquivo.length() == 0;

        try (FileWriter escritor = new FileWriter(arquivo, true)) {

            if (arquivoNaoExiste) {
                escritor.write("ID;Nome;CPF;Email;Endereco;Login;Senha;Perfil;");
                escritor.write("Matricula;Periodo;DataEmissao;Receita;Despesas;Salario;\n");
            }

            escritor.write(idUsuarioString + ";");
            escritor.write(usuario.getNome() + ";");
            escritor.write(usuario.getCpf() + ";");
            escritor.write(usuario.getEmail() + ";");
            escritor.write(usuario.getEndereco() + ";");
            escritor.write(usuario.getLogin() + ";");
            escritor.write(usuario.getSenha() + ";");
            escritor.write(usuario.getPerfil() + ";");

            if (usuario instanceof Funcionario funcionario) {
                escritor.write(funcionario.getMatricula() + ";");
                escritor.write(funcionario.getPeriodo() + ";");
                escritor.write(funcionario.getDataEmissao().toString() + ";");
                escritor.write(funcionario.getReceita() + ";");
                escritor.write(funcionario.getDespesas() + ";");
                escritor.write(funcionario.getSalario() + ";");
            } else {
                escritor.write(";;;;;;");
            }

            escritor.write("\n");
        }
    }
}