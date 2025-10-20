package app.humanize;

import app.humanize.model.*;
import app.humanize.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDate;


public class SetupUsuariosComHash {

    public static void main(String[] args) {
        System.out.println("Iniciando a criação de usuários com senhas seguras...");

        UsuarioRepository usuarioRepository = new UsuarioRepository();

        try {
            // --- 1. Criar Funcionário (Ana) ---
            String senhaAna = "ana123";
            String hashAna = BCrypt.hashpw(senhaAna, BCrypt.gensalt());
            System.out.println("Gerado hash para Ana: " + hashAna);

            Endereco enderecoAna = new Endereco.EnderecoBuilder()
                    .logradouro("Rua das Flores").numero(123).bairro("Centro")
                    .cidade("São Paulo").estado("SP").cep("01000-000").build();

            Funcionario ana = new Funcionario.FuncionarioBuilder()
                    .nome("Ana Silva").cpf("111.222.333-44").email("ana.silva@email.com")
                    .endereco(enderecoAna).login("ana").senha(hashAna) // <-- Usando o hash!
                    .perfil(Perfil.FUNCIONARIO).matricula(101010).periodo(8)
                    .dataEmissao(LocalDate.of(2023, 5, 15))
                    .receita(5000.0).despesas(1500.0).salario(3500.0).build();
            ana.setId(1);
            usuarioRepository.escreveUsuarioNovo(ana);
            System.out.println("Usuário 'ana' criado.");


            // --- 2. Criar Administrador (Carlos) ---
            String senhaCarlos = "carlos123";
            String hashCarlos = BCrypt.hashpw(senhaCarlos, BCrypt.gensalt());
            System.out.println("Gerado hash para Carlos: " + hashCarlos);

            Endereco enderecoCarlos = new Endereco.EnderecoBuilder()
                    .logradouro("Avenida Principal").numero(789).bairro("Norte")
                    .cidade("Rio de Janeiro").estado("RJ").cep("20000-000").build();

            Administrador carlos = new Administrador.AdministradorBuilder()
                    .nome("Carlos Souza").cpf("555.666.777-88").email("carlos.souza@email.com")
                    .endereco(enderecoCarlos).login("carlos").senha(hashCarlos) // <-- Usando o hash!
                    .perfil(Perfil.ADMINISTRADOR).build();
            carlos.setId(2);
            usuarioRepository.escreveUsuarioNovo(carlos);
            System.out.println("Usuário 'carlos' criado.");


            // --- 3. Criar Gestor (Jorge) ---
            String senhaJorge = "jorge123";
            String hashJorge = BCrypt.hashpw(senhaJorge, BCrypt.gensalt());
            System.out.println("Gerado hash para Jorge: " + hashJorge);

            Gestor jorge = new Gestor.GestorBuilder()
                    .nome("Jorge Paiva").cpf("123.456.789-00").email("jorge.paiva@email.com")
                    .endereco(enderecoAna) // Reutilizando o endereço da Ana como exemplo
                    .login("jorge").senha(hashJorge) // <-- Usando o hash!
                    .perfil(Perfil.GESTOR).build();
            jorge.setId(3);
            usuarioRepository.escreveUsuarioNovo(jorge);
            System.out.println("Usuário 'jorge' criado.");


            // --- 4. Criar Recrutador (Maria) ---
            String senhaMaria = "maria123";
            String hashMaria = BCrypt.hashpw(senhaMaria, BCrypt.gensalt());
            System.out.println("Gerado hash para Maria: " + hashMaria);

            Recrutador maria = new Recrutador.RecrutadorBuilder()
                    .nome("Maria Oliveira").cpf("987.654.321-11").email("maria.oliveira@email.com")
                    .endereco(enderecoCarlos) // Reutilizando o endereço do Carlos como exemplo
                    .login("maria").senha(hashMaria) // <-- Usando o hash!
                    .perfil(Perfil.RECRUTADOR).build();
            maria.setId(4);
            usuarioRepository.escreveUsuarioNovo(maria);
            System.out.println("Usuário 'maria' criado.");


            System.out.println("\nArquivo 'usuarios.csv' gerado/atualizado com sucesso!");
            System.out.println("Tente logar com os usuários e suas senhas originais.");

        } catch (IOException e) {
            System.err.println("Ocorreu um erro ao escrever no arquivo CSV.");
            e.printStackTrace();
        }
    }
}
