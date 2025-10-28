package app.humanize.repository;

import app.humanize.model.*;
import app.humanize.util.EstadosBrasileiros;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsuarioRepository {

    private static final UsuarioRepository instance = new UsuarioRepository();
    private final String arquivoCsv = "./src/main/resources/usuarios.csv";
    private final List<Usuario> usuariosEmMemoria;

    private UsuarioRepository() {
        this.usuariosEmMemoria = new ArrayList<>();
        this.carregarUsuariosDoCSV();
    }

    public static UsuarioRepository getInstance() {
        return instance;
    }

    public List<Usuario> getTodosUsuarios() {
        return new ArrayList<>(this.usuariosEmMemoria);
    }

    public List<Usuario> getRecrutadores() {
        List<Usuario> recrutadores = new ArrayList<>();
        for(Usuario usuario : this.usuariosEmMemoria) {
            if (usuario instanceof Recrutador) {
                recrutadores.add(usuario);
            }
        }
        return recrutadores;
    }

    public List<Usuario> getFuncionarios() {
        return this.usuariosEmMemoria.stream()
                .filter(usuario -> usuario.getPerfil() == Perfil.FUNCIONARIO)
                .collect(Collectors.toList());
    }

    public Optional<Usuario> buscaUsuarioPorLogin(String login) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login))
                .findFirst();
    }

    public Optional<Usuario> buscaUsuarioPorId(int id) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    public Optional<Usuario> buscaPorCpf(String cpf) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getCpf().equalsIgnoreCase(cpf))
                .findFirst();
    }

    public List<Usuario> buscaPorPerfil(Perfil perfil) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getPerfil().equals(perfil))
                .toList();
    }

    public int getQtdUsuarios(){
        return this.usuariosEmMemoria.size();
    }

    public void escreveUsuarioNovo(Usuario usuario) throws IOException {
        int proximoId = getProximoId();
        usuario.setId(proximoId);

        this.usuariosEmMemoria.add(usuario);
        this.persistirAlteracoesNoCSV();
    }

    public int getProximoId() {
        return this.usuariosEmMemoria.stream()
                .mapToInt(Usuario::getId)
                .max()
                .orElse(0)
                + 1;
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
            // CABEÇALHO ATUALIZADO (17 COLUNAS)
            escritor.write("ID;Nome;CPF;Email;Endereco;Login;Senha;Perfil;Matricula;Periodo;Receita;Despesas;Salario;Cargo;Regime;Departamento;CaminhoFoto;\n");
            for (Usuario usuario : this.usuariosEmMemoria) {
                escritor.write(formatarUsuarioParaCSV(usuario));
            }
        }
    }

    private Usuario parseUsuarioDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        // ATUALIZADO: ESPERA 17 CAMPOS
        if (campos.length < 17) {
            System.err.println("Linha CSV inválida (poucos campos, esperado 17): " + linha);
            return null;
        }

        try {
            int id = Integer.parseInt(campos[0]);
            String nome = campos[1];
            String cpf = campos[2];
            String email = campos[3];
            String login = campos[5];
            String senha = campos[6];
            Perfil perfil = Perfil.valueOf(campos[7].trim().toUpperCase());

            Endereco endereco = null;
            if (!campos[4].trim().isEmpty()) {
                String[] partesEndereco = campos[4].split(",");
                if (partesEndereco.length == 6) {
                    endereco = new Endereco.EnderecoBuilder()
                            .logradouro(partesEndereco[0].trim())
                            .numero(Integer.parseInt(partesEndereco[1].trim()))
                            .bairro(partesEndereco[2].trim())
                            .cidade(partesEndereco[3].trim())
                            .estado(EstadosBrasileiros.valueOf(partesEndereco[4].trim().toUpperCase()))
                            .cep(partesEndereco[5].trim())
                            .build();
                } else {
                    System.err.println("Formato de endereço inválido na linha: " + linha);
                }
            }

            int matricula = Integer.parseInt(campos[8].trim().isEmpty() ? "0" : campos[8].trim());
            int periodo = Integer.parseInt(campos[9].trim().isEmpty() ? "0" : campos[9].trim());
            double receita = Double.parseDouble(campos[10].trim().isEmpty() ? "0.0" : campos[10].trim());
            double despesas = Double.parseDouble(campos[11].trim().isEmpty() ? "0.0" : campos[11].trim());
            double salario = Double.parseDouble(campos[12].trim().isEmpty() ? "0.0" : campos[12].trim());
            String cargo = campos[13].trim();

            Regime regime = null;
            String regimeStr = campos[14].trim();
            if (!regimeStr.isEmpty() && !regimeStr.equalsIgnoreCase("null")) {
                regime = Regime.valueOf(regimeStr.toUpperCase());
            }

            String departamento = campos[15].trim();
            String caminhoFoto = campos[16].trim(); // NOVO CAMPO LIDO

            Usuario usuario = switch (perfil) {
                case ADMINISTRADOR -> new Administrador.AdministradorBuilder()
                        .nome(nome).cpf(cpf).email(email).endereco(endereco)
                        .login(login).senha(senha).perfil(perfil)
                        .matricula(matricula).periodo(periodo).departamento(departamento)
                        .receita(receita).despesas(despesas).salario(salario)
                        .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto) // NOVO
                        .build();
                case GESTOR -> new Gestor.GestorBuilder()
                        .nome(nome).cpf(cpf).email(email).endereco(endereco)
                        .login(login).senha(senha).perfil(perfil)
                        .matricula(matricula).periodo(periodo).departamento(departamento)
                        .receita(receita).despesas(despesas).salario(salario)
                        .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto) // NOVO
                        .build();
                case RECRUTADOR -> new Recrutador.RecrutadorBuilder()
                        .nome(nome).cpf(cpf).email(email).endereco(endereco)
                        .login(login).senha(senha).perfil(perfil)
                        .matricula(matricula).periodo(periodo).departamento(departamento)
                        .receita(receita).despesas(despesas).salario(salario)
                        .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto) // NOVO
                        .build();
                default ->
                        new Funcionario.FuncionarioBuilder()
                                .nome(nome).cpf(cpf).email(email).endereco(endereco)
                                .login(login).senha(senha).perfil(perfil)
                                .matricula(matricula).periodo(periodo).departamento(departamento)
                                .receita(receita).despesas(despesas).salario(salario)
                                .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto) // NOVO
                                .build();
            };

            usuario.setId(id);
            return usuario;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String formatarUsuarioParaCSV(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        sb.append(usuario.getId()).append(";");
        sb.append(usuario.getNome()).append(";");
        sb.append(usuario.getCpf()).append(";");
        sb.append(usuario.getEmail()).append(";");

        if (usuario.getEndereco() != null) {
            Endereco endereco = usuario.getEndereco();
            sb.append(String.join( ",",
                    endereco.getLogradouro(),
                    String.valueOf(endereco.getNumero()),
                    endereco.getBairro(),
                    endereco.getCidade(),
                    String.valueOf(endereco.getEstado()) ,
                    endereco.getCep()
            ));
        }
        sb.append(";");

        sb.append(usuario.getLogin()).append(";");
        sb.append(usuario.getSenha()).append(";");
        sb.append(usuario.getPerfil()).append(";");

        if (usuario instanceof Funcionario f) {
            sb.append(f.getMatricula()).append(";");
            sb.append(f.getPeriodo()).append(";");
            sb.append(f.getReceita()).append(";");
            sb.append(f.getDespesas()).append(";");
            sb.append(f.getSalario()).append(";");
            sb.append(f.getCargo() == null ? "" : f.getCargo()).append(";");
            sb.append(f.getRegime() == null ? "" : f.getRegime().name()).append(";");
            sb.append(f.getDepartamento() == null ? "" : f.getDepartamento()).append(";");
            sb.append(f.getCaminhoFoto() == null ? "" : f.getCaminhoFoto()).append(";"); // NOVO CAMPO ESCRITO
        } else {
            sb.append(";;;;;;;;;"); // ATUALIZADO: 9 colunas vazias
        }
        sb.append("\n");
        return sb.toString();
    }

    public void excluirUsuario(Usuario usuarioParaExcluir) throws IOException {
        if (usuarioParaExcluir == null) {
            return;
        }
        boolean removido = this.usuariosEmMemoria.removeIf(usuario -> usuario.getId() == usuarioParaExcluir.getId());
        if (removido) {
            persistirAlteracoesNoCSV();
        }
    }

    public void atualizarUsuario(Usuario usuarioAtualizado) throws IOException {
        Optional<Usuario> usuarioAntigoOpt = buscaUsuarioPorId(usuarioAtualizado.getId());

        if (usuarioAntigoOpt.isPresent()) {
            int index = this.usuariosEmMemoria.indexOf(usuarioAntigoOpt.get());
            if (index != -1) {
                this.usuariosEmMemoria.set(index, usuarioAtualizado);
                persistirAlteracoesNoCSV();
            } else {
                throw new IOException("Erro interno: Usuário encontrado mas índice não localizado.");
            }
        } else {
            throw new IOException("Usuário com ID " + usuarioAtualizado.getId() + " não encontrado para atualizar.");
        }
    }
}