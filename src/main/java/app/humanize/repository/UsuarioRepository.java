package app.humanize.repository;

import app.humanize.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    private final String arquivoCsv = "./src/main/resources/usuarios.csv";
    private final List<Usuario> usuariosEmMemoria;

    public UsuarioRepository() {
        this.usuariosEmMemoria = new ArrayList<>();
        this.carregarUsuariosDoCSV();
    }

    public List<Usuario> getTodosUsuarios() {
        return new ArrayList<>(this.usuariosEmMemoria);
    }

    public Optional<Usuario> buscaUsuarioPorId(int id) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    public Optional<Usuario> buscaUsuarioPorLogin(String login) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login))
                .findFirst();
    }

    public void escreveUsuarioNovo(Usuario usuario) throws IOException {
        if (buscaUsuarioPorId(usuario.getId()).isPresent()) {
            System.out.println("Usuário já cadastrado (ID: " + usuario.getId() + ")");
            return;
        }
        this.usuariosEmMemoria.add(usuario);
        this.persistirAlteracoesNoCSV();
    }

    private void carregarUsuariosDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) {
            return;
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                Usuario usuario = parseUsuarioDaLinhaCsv(linha);
                if (usuario != null) {
                    this.usuariosEmMemoria.add(usuario);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar usuários do arquivo CSV: " + e.getMessage());
        }
    }

    private void persistirAlteracoesNoCSV() throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("ID;Nome;CPF;Email;Endereco;Login;Senha;Perfil;Matricula;Periodo;DataEmissao;Receita;Despesas;Salario;\n");
            for (Usuario usuario : this.usuariosEmMemoria) {
                escritor.write(formatarUsuarioParaCSV(usuario));
            }
        }
    }

    private Usuario parseUsuarioDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 14) return null; // Linha inválida

        try {
            int id = Integer.parseInt(campos[0]);
            String nome = campos[1];
            String cpf = campos[2];
            String email = campos[3];
            String login = campos[5];
            String senha = campos[6];

            Perfil perfil = Perfil.valueOf(campos[7]);
            String[] partesEndereco = campos[4].split(",");
            String logradouro = partesEndereco[0].trim();
            int numero = Integer.parseInt(partesEndereco[1].trim());
            String bairro = partesEndereco[2].trim();
            String cidade = partesEndereco[3].trim();
            String estado = partesEndereco[4].trim();
            String cep = partesEndereco[5].trim();

            Endereco endereco = new Endereco.EnderecoBuilder()
                    .logradouro(logradouro)
                    .numero(numero)
                    .bairro(bairro)
                    .cidade(cidade)
                    .estado(estado)
                    .cep(cep)
                    .build();
            Usuario usuario;

            if (perfil == Perfil.FUNCIONARIO) {
                Funcionario funcionario = new Funcionario.FuncionarioBuilder()
                        .nome(campos[1])
                        .cpf(campos[2])
                        .email(campos[3])
                        .endereco(endereco)
                        .login(campos[5])
                        .senha(campos[6])
                        .perfil(perfil)
                        .matricula(Integer.parseInt(campos[8]))
                        .periodo(Integer.parseInt(campos[9]))
                        .dataEmissao(LocalDate.parse(campos[10]))
                        .receita(Double.parseDouble(campos[11]))
                        .despesas(Double.parseDouble(campos[12]))
                        .salario(Double.parseDouble(campos[13]))
                        .build();

                funcionario.setId(Integer.parseInt(campos[0]));

                return funcionario;
            }
            else if (perfil == Perfil.ADMINISTRADOR) {
                Administrador administrador = new Administrador.AdministradorBuilder().nome(campos[1])
                        .cpf(campos[2])
                        .email(campos[3])
                        .endereco(endereco)
                        .login(campos[5])
                        .senha(campos[6])
                        .perfil(perfil)
                        .build();
                administrador.setId(Integer.parseInt(campos[0]));
                return administrador;
            }
            else if (perfil == Perfil.GESTOR) {
                Gestor gestor = new Gestor.GestorBuilder().nome(campos[1])
                        .cpf(campos[2])
                        .email(campos[3])
                        .endereco(endereco)
                        .login(campos[5])
                        .senha(campos[6])
                        .perfil(perfil)
                        .build();
                gestor.setId(Integer.parseInt(campos[0]));
                return gestor;
            }
            else if (perfil == Perfil.RECRUTADOR) {
                Recrutador recrutador = new Recrutador.RecrutadorBuilder().nome(campos[1])
                        .cpf(campos[2])
                        .email(campos[3])
                        .endereco(endereco)
                        .login(campos[5])
                        .senha(campos[6])
                        .perfil(perfil)
                        .build();
                recrutador.setId(Integer.parseInt(campos[0]));
                return recrutador;
            }

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
        }
        return null;
    }

    private String formatarUsuarioParaCSV(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        sb.append(usuario.getId()).append(";");
        sb.append(usuario.getNome()).append(";");
        sb.append(usuario.getCpf()).append(";");
        sb.append(usuario.getEmail()).append(";");

        if (usuario.getEndereco() != null) {
            Endereco end = usuario.getEndereco();
            String enderecoFormatado = String.join(",",
                    end.getLogradouro(), String.valueOf(end.getNumero()), end.getBairro(),
                    end.getCidade(), end.getEstado(), end.getCep());
            sb.append(enderecoFormatado);
        }
        sb.append(";");

        sb.append(usuario.getLogin()).append(";");
        sb.append(usuario.getSenha()).append(";");
        sb.append(usuario.getPerfil()).append(";");

        if (usuario instanceof Funcionario f) {
            sb.append(f.getMatricula()).append(";");
            sb.append(f.getPeriodo()).append(";");
            sb.append(f.getDataEmissao()).append(";");
            sb.append(f.getReceita()).append(";");
            sb.append(f.getDespesas()).append(";");
            sb.append(f.getSalario()).append(";");
        } else {
            sb.append(";;;;;;");
        }
        sb.append("\n");
        return sb.toString();
    }
}

