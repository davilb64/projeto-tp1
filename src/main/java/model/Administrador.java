package model;

public class Administrador extends Usuario {
    //Construtor

    public Administrador(int id, String nome, String cpf, Endereco endereco, String email, String login, String senha) {
        super(id, nome, cpf, endereco, email, login, senha);
    }

    //metodos
    public Usuario cadastrarUsuario(int id, String nome, String cpf, Endereco endereco, String email, String login, String senha, String cargo) {
        if(cargo.equals("administrador")){
            return new Administrador(id,nome,cpf,endereco,email,login,senha);
        } else if (cargo.equals("gestor")) {
           return new Gestor(id,nome,cpf,endereco,email,login,senha); 
        } else if (cargo.equals("recrutador")) {
            return new Recrutador();
        } else if (cargo.equals("funcionario")) {
            return new Funcionario();
        }
        return null;
    }
}
