# Pacote: controller

Este pacote contém **os controladores do sistema**, responsáveis por intermediar a comunicação entre a **view** (interface gráfica) e o **model** (regras de negócio).

## Funções principais:
- Receber ações do usuário (botões, cliques, formulários);
- Validar dados antes de enviá-los para o `model`;
- Atualizar a interface após operações no sistema;
- Centralizar a lógica de navegação entre as telas.

## Exemplos de classes:
- `LoginController` → Controla a tela de login.
- `DashboardController` → Controla o menu principal.
- `CandidatosController` → CRUD de candidatos.
- `VagasController` → CRUD de vagas e entrevistas.
- `FinanceiroController` → Gerencia folha de pagamento.
- `PrestadoresController` → CRUD de prestadores.
