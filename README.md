
# 🏢 Sistema de Gestão de Recursos Humanos — Grupo 07 (UnB)

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Maven](https://img.shields.io/badge/build-Maven-blue)
![Java](https://img.shields.io/badge/java-17-red)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 📌 Sobre o Projeto
Este é o **Trabalho Prático** da disciplina de **Técnicas de Programação 1** da **UnB**.  
O objetivo do sistema é **gerenciar candidatos, vagas, entrevistas, funcionários, prestadores de serviço e folha de pagamento**, oferecendo uma interface interativa e relatórios completos.

---

## 📂 Estrutura do Projeto
O projeto utiliza **Maven** e segue uma **arquitetura MVC** com camadas organizadas:

projetoRH/<br>
├── src/main/java/<br>
│   ├── view/             # Interfaces gráficas (JavaFX)<br>
│   ├── controller/       # Lógica de controle e integração<br>
│   ├── model/            # Entidades e regras de negócio<br>
│   ├── repository/       # Persistência de dados (arquivos e CSV)<br>
│   ├── util/             # Validações, helpers e cálculos<br>
│   └── Main.java         # Ponto de entrada do sistema<br>
├── src/main/resources/   # Arquivos de configuração e assets<br>
├── pom.xml               # Configurações do Maven<br>
└── README.md<br>

---

## 🚀 Funcionalidades Principais
- 🔐 **Autenticação** com diferentes níveis de acesso: Administrador, Gestor, Recrutador e Funcionário.
- 👨‍💼 **Gestão de candidatos** — cadastro, atualização, exclusão e listagem.
- 📝 **Gerenciamento de vagas e entrevistas**.
- 💰 **Controle financeiro** — folha de pagamento e relatórios.
- 🧾 **Gestão de prestadores e contratos**.
- 📊 **Geração de relatórios** em **Excel, PDF e CSV**.
- 🎨 Interface intuitiva com **JavaFX**.

---

## 🛠️ Tecnologias Utilizadas
- **Java 17** → Linguagem principal
- **JavaFX** → Interface gráfica
- **Maven** → Gerenciamento de dependências
- **JUnit 5** → Testes unitários
- **Git Flow** → Gerenciamento de branches

---

## ⚡ Configuração do Ambiente
### **Pré-requisitos**
- [Java 17+](https://jdk.java.net/)
- [Maven](https://maven.apache.org/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Git](https://git-scm.com/)

### **Instalação**
```bash
# Clonar o repositório
git clone https://github.com/davilb64/projeto-tp1.git

# Entrar na pasta do projeto
cd projeto-tp1

# Compilar o projeto
mvn clean install

# Executar o projeto
mvn javafx:run
```

---

## 🗂️ Roadmap do Desenvolvimento
| **Etapa** | **Descrição**                                | **Prazo**    | **Responsáveis**         |
|-----------|--------------------------------------------|------------|--------------------------|
| **Etapa 1** | Estrutura inicial, protótipos e diagramas UML | 16/09/2025 | Todos                   |
| **Etapa 2** | Modelagem completa e assinaturas de métodos  | 30/09/2025 | Todos                   |
| **Etapa 3** | CRUDs, validações, persistência e testes    | 28/10/2025 | Cada aluno por módulo   |
| **Etapa 4** | Integração final e relatórios               | 11/11/2025 | Todos                   |
| **Etapa 5** | Entrega final, manual e apresentação        | 28/11/2025 | Todos                   |

---

## 🧑‍💻 Contribuidores
| Aluno | Módulo | GitHub |
|--------|-------------------------------|-------------------------|
| **Aluno 1** | Administração e autenticação | [@aluno1](https://github.com/aluno1) |
| **Aluno 2** | Candidatos e candidaturas    | [@aluno2](https://github.com/aluno2) |
| **Aluno 3** | Vagas, entrevistas, contratações | [@aluno3](https://github.com/aluno3) |
| **Aluno 4** | Financeiro e folha de pagamento | [@aluno4](https://github.com/aluno4) |
| **Aluno 5** | Prestadores e contratos     | [@aluno5](https://github.com/aluno5) |

---

## 📜 Licença
Este projeto é distribuído sob a licença **MIT**.  
Sinta-se livre para modificar e utilizar, desde que mantenha os créditos.

---

## 🌟 Observações
- Utilize **Git Flow** para criar features, releases e hotfixes.
- Sempre crie **pull requests** para integração de novas funcionalidades.
- Use o [Trello do projeto]([https://trello.com/](https://trello.com/b/EUpSU1r6/projeto-tp1)) para acompanhar tarefas.

---
Feito com ❤️ pelo **Grupo 07** — UnB, 2025.
