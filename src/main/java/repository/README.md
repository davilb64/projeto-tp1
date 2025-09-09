# Pacote: repository

Este pacote contém as **classes responsáveis pela persistência de dados** do sistema.  
É aqui que salvamos, buscamos, atualizamos e excluímos dados em **arquivos, CSV, JSON ou banco de dados**.

## Funções principais:
- Salvar dados de candidatos, vagas, usuários e prestadores;
- Carregar dados salvos para a memória;
- Exportar relatórios em CSV, JSON e PDF (opcional);
- Garantir consistência das informações persistidas.

## Exemplos de classes:
- `UsuarioRepository` → Persiste dados de usuários.
- `CandidatoRepository` → Persiste dados de candidatos.
- `VagaRepository` → Persiste vagas e entrevistas.
- `FinanceiroRepository` → Persiste folha de pagamento.
