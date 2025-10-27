package app.humanize.repository;

import app.humanize.model.*;
import app.humanize.util.EstadosBrasileiros;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            // 1. ADICIONADO "Cargo" AO CABEÇALHO
            escritor.write("ID;Nome;CPF;Email;Endereco;Login;Senha;Perfil;Matricula;Periodo;DataEmissao;Receita;Despesas;Salario;Cargo;Regime;\n");
            for (Usuario usuario : this.usuariosEmMemoria) {
                escritor.write(formatarUsuarioParaCSV(usuario));
            }
        }
    }

    private Usuario parseUsuarioDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        // 2. NÚMERO DE CAMPOS AUMENTADO PARA 16
        if (campos.length < 16) {
            System.err.println("Linha CSV inválida (poucos campos, esperado 16): " + linha);
            return null;
        }

        try {
            int id = Integer.parseInt(campos[0]);
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

            Usuario usuario;
            if (perfil == Perfil.FUNCIONARIO) {

                LocalDate dataEmissao = null;
                String dataEmissaoStr = campos[10].trim();
                if (!dataEmissaoStr.isEmpty() && !dataEmissaoStr.equalsIgnoreCase("null")) {
                    try {
                        dataEmissao = LocalDate.parse(dataEmissaoStr);
                    } catch (java.time.format.DateTimeParseException dtpe) {
                        System.err.println("Formato de data inválido na linha: '" + linha + "'. Campo data: '" + dataEmissaoStr + "'");
                        return null;
                    }
                }

                String cargo = campos[14].trim();

                Regime regime = null;
                String regimeStr = campos[15].trim();
                if (!regimeStr.isEmpty()) {
                    try {
                        regime = Regime.valueOf(regimeStr.toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        System.err.println("Valor de Regime inválido na linha: '" + linha + "'. Campo regime: '" + regimeStr + "'");
                    }
                }

                Funcionario.FuncionarioBuilder builder = new Funcionario.FuncionarioBuilder()
                        .matricula(Integer.parseInt(campos[8].trim().isEmpty() ? "0" : campos[8].trim()))
                        .periodo(Integer.parseInt(campos[9].trim().isEmpty() ? "0" : campos[9].trim()))
                        .dataEmissao(dataEmissao)
                        .receita(Double.parseDouble(campos[11].trim().isEmpty() ? "0.0" : campos[11].trim()))
                        .despesas(Double.parseDouble(campos[12].trim().isEmpty() ? "0.0" : campos[12].trim()))
                        .salario(Double.parseDouble(campos[13].trim().isEmpty() ? "0.0" : campos[13].trim()))
                        .cargo(cargo) // 4. ADICIONADO AO BUILDER
                        .regime(regime);

                usuario = builder.build();

            } else if (perfil == Perfil.ADMINISTRADOR) {
                usuario = new Administrador.AdministradorBuilder().build();
            } else if (perfil == Perfil.GESTOR) {
                usuario = new Gestor.GestorBuilder().build();
            } else { // RECRUTADOR
                usuario = new Recrutador.RecrutadorBuilder().build();
            }

            usuario.setId(id);
            usuario.setNome(campos[1]);
            usuario.setCpf(campos[2]);
            usuario.setEmail(campos[3]);
            usuario.setEndereco(endereco);
            usuario.setLogin(campos[5]);
            usuario.setSenha(campos[6]);
            usuario.setPerfil(perfil);

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
            sb.append(String.join( ",", endereco.getLogradouro(), String.valueOf(endereco.getNumero()), endereco.getBairro(), endereco.getCidade(), String.valueOf(endereco.getEstado()) , endereco.getCep()));
        }
        sb.append(";");

        sb.append(usuario.getLogin()).append(";");
        sb.append(usuario.getSenha()).append(";");
        sb.append(usuario.getPerfil()).append(";");

        if (usuario instanceof Funcionario f) {
            sb.append(f.getMatricula()).append(";");
            sb.append(f.getPeriodo()).append(";");
            sb.append(f.getDataEmissao() == null ? "" : f.getDataEmissao().toString()).append(";");
            sb.append(f.getReceita()).append(";");
            sb.append(f.getDespesas()).append(";");
            sb.append(f.getSalario()).append(";");
            sb.append(f.getCargo() == null ? "" : f.getCargo()).append(";");
            sb.append(f.getRegime() == null ? "" : f.getRegime().name()).append(";");
        } else {
            sb.append(";;;;;;;;");
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