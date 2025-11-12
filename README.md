
# 🏢 Humanize — Grupo 07

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Maven](https://img.shields.io/badge/build-Maven-blue)
![Java](https://img.shields.io/badge/java-22-red)
![Static Badge](https://img.shields.io/badge/UI-JavaFX-blue)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 📌 Sobre o Projeto
Este é o **Trabalho Prático** da disciplina de **Técnicas de Programação 1** da **UnB**.  
O objetivo do sistema é **automatizar os principais processos relacionados aos colaboradores em uma organização de recursos humanos**, oferecendo uma interface interativa e relatórios completos.

---

## 📂 Estrutura do Projeto
O projeto utiliza **Maven** e segue uma **arquitetura MVC** com camadas organizadas:

projeto-tp1/
<br>├── documentação/          # Armazena relatórios do projeto
<br>├── empacotados/          # Armazena executáveis do projeto
<br>├── src/main/java/
<br>│   └── app/humanize/
<br>│       ├── controller/    # Controladores JavaFX (lógica da UI)
<br>│       ├── exceptions/    # Exceções customizadas (ex: CpfInvalidoException)
<br>│       ├── model/         # Classes de Domínio (Usuario, Funcionario, Vaga, etc.)
<br>│       ├── repository/    # Camada de Persistência (Leitura/Escrita de CSV)
<br>│       ├── service/       # Camada de Aplicação (Lógica de Negócio)
<br>│       │   ├── formatters/  # Padrão Strategy: Formatadores (PdfFormatter, CsvFormatter)
<br>│       │   ├── relatorios/  # Padrão Strategy: Geradores de dados (RelatorioListaUsuarios)
<br>│       │   └── validacoes/  # Serviços de validação (ValidaCpf, ValidaEmail)
<br>│       ├── util/          # Classes utilitárias (UserSession, ScreenController)
<br>│       ├── Launcher.java  # Ponto de entrada do app
<br>│       └── Main.java      # Classe principal da aplicação JavaFX
<br>│
<br>├── src/main/resources/
<br>│   ├── bundles/         # Arquivos de tradução
<br>│   ├── css/             # StyleSheets do css
<br>│   ├── fotos_perfil/    # Fotos de perfil dos usuários
<br>│   ├── uploads/         # (Reservado para uploads de documentos/currículos)
<br>│   └── view/            # Telas da aplicação (arquivos FXML)
<br>│
<br>├── pom.xml                # Dependências e build do Maven
<br>└── README.md              # Esta documentação

---

## 🚀 Funcionalidades Principais
- 🔐 **Autenticação** com diferentes níveis de acesso: Administrador, Gestor, Recrutador e Funcionário.
- 👨‍💼 **Gestão de candidatos** — cadastro, atualização, exclusão e listagem.
- 📝 **Gerenciamento de vagas e entrevistas**.
- 💰 **Controle financeiro** — folha de pagamento e relatórios.
- 🧾 **Gestão de prestadores e contratos**.
- 📊 **Geração de relatórios** em **PDF e CSV**.
- 🎨 Interface intuitiva com **JavaFX**.
- E muito mais...

---

## 🛠️ Tecnologias Utilizadas
- **Java 22** → Linguagem principal
- **JavaFX** → Interface gráfica
- **Maven** → Gerenciamento de dependências
- **Git Flow** → Gerenciamento de branches

---

## ⚡ Configuração do Ambiente
### **Pré-requisitos**
- [Java 22](https://jdk.java.net/)
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

# Executar "src/main/java/app/humanize/Launcher.java"
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
| **Aluno 1** | Administração e Gestão | [Davi Lopes](https://github.com/davilb64) |
| **Aluno 2** | Candidatura    | [Valquíria Machado](https://github.com/valquiria11) |
| **Aluno 3** | Recrutamento | [Ricado Rian](https://github.com/RianRSM) |
| **Aluno 4** | Financeiro | [Samara Gomes](https://github.com/samaragomess) |


---

## 📜 Licença
Este projeto é distribuído sob a licença **MIT**.  
Sinta-se livre para modificar e utilizar, desde que mantenha os créditos.

---

## 🌟 Observações
- Utilize **Git Flow** para criar features, releases e hotfixes.

---
Feito com ❤️ pelo **Grupo 07** — UnB, 2025.
