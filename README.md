
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

projeto-tp1/<br>
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
| **Etapa**   | **Descrição**                | **Prazo**  | **Responsáveis**       |
|-------------|------------------------------|------------|------------------------|
| **Etapa 1** | Estrutura inicial            | 23/09/2025 | Todos                  |
| **Etapa 2** | Design e Modelagem           | 07/10/2025 | Todos                  |
| **Etapa 3** | Implementaçao da Logica      | 28/10/2025 | Cada membro por módulo |
| **Etapa 4** | Integração e Navegabilidade  | 11/11/2025 | Todos                  |
| **Etapa 5** | Entrega final e apresentação | 25/11/2025 | Todos                  |

---

## 🧑‍💻 Contribuidores
| Aluno | Módulo | GitHub |
|--------|-------------------------------|-------------------------|
| **Aluno 1** | Administração e autenticação | [Davi Lopes](https://github.com/davilb64) |
| **Aluno 2** | Candidatos e candidaturas    | [Valquíria Machado](https://github.com/valquiria11) |
| **Aluno 3** | Vagas, entrevistas, contratações | [Ricado Rian](https://github.com/RianRSM) |
| **Aluno 4** | Financeiro e folha de pagamento | [Samara Gomes](https://github.com/samaragomess) |


---

## 📜 Licença
Este projeto é distribuído sob a licença **MIT**.  
Sinta-se livre para modificar e utilizar, desde que mantenha os créditos.

---

## 🌟 Observações
- Utilize **Git Flow** para criar features, releases e hotfixes.
- Sempre crie **pull requests** para integração de novas funcionalidades.

---
Feito com ❤️ pelo **Grupo 07** — UnB, 2025.
