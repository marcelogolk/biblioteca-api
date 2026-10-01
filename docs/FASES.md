# 📅 Fases de Desenvolvimento

## Visão Geral

O projeto será desenvolvido de forma incremental, com releases versionadas. Cada fase adiciona funcionalidades e melhora a qualidade do código.

---

## ✅ Fase 1: MVP (v1.0.0)

**Status:** Em desenvolvimento

**Objetivo:** Implementar o CRUD básico com arquitetura em camadas e regras essenciais de negócio.

### Funcionalidades

- [x] Estrutura do projeto Quarkus
- [x] Entidade `Proprietario` (Id, Nome, CPF como Chave de Negócio)
- [x] Entidade `Livro` (Id, Título, Autor, Categoria, Ano, Proprietário)
- [x] Repository com Panache para entidades
- [x] Service com lógica de negócio (CPF imutável após cadastro, sem alteração via atualização)
- [x] Controller REST para Proprietário com endpoints:
  - `POST /api/proprietarios` — Criar proprietário
  - `GET /api/proprietarios` — Listar todos
  - `GET /api/proprietarios/{id}` — Buscar por ID
  - `PUT /api/proprietarios/{id}` — Atualizar
  - `DELETE /api/proprietarios/{id}` — Deletar
- [x] Controller REST para Livros:
  - `POST /api/livros` — Criar livro
  - `GET /api/livros` — Listar todos
  - `GET /api/livros/{id}` — Buscar por ID
  - `GET /api/livros/categoria/{categoria}` — Buscar por categoria
  - `GET /api/livros/proprietario/{proprietarioId}` — Buscar por proprietário
  - `PUT /api/livros/{id}` — Atualizar
  - `DELETE /api/livros/{id}` — Deletar
- [x] Validações de formato básicas via Bean Validation (`@NotBlank`, `@Size`, `@Pattern`)
- [x] Tratamento de erros de negócio e concorrência (400, 404, 409, 500)
- [~] Testes unitários (70% cobertura) — em andamento:
  - [x] Testes de entidade (`ProprietarioTest`, `LivroTest`, `YearAttributeConverterTest`)
  - [x] Testes de integração do `LivroController` (`LivroControllerTest`, incluindo inclusão, atualização e exclusão)
  - [ ] Testes de integração do `ProprietarioController` (estrutura criada, implementação pendente)
  - [ ] Testes unitários do `LivroService` com mocks (estrutura criada, implementação pendente)
  - [ ] Teste do cenário "lista vazia" em `listAllLivros` (depende de decisão sobre isolamento do banco de testes)
- [x] Documentação Swagger UI
- [x] Containerização do banco de dados (Postgres via Docker Compose, local e produção)
- [ ] Deploy em VPS Oracle Cloud (camadas 0 a 5 concluídas; containerização da API e exposição à internet pendentes)

### Arquitetura
controller/ → service/ → repository/ → banco de dados

### Pendências conhecidas da Fase 1

- Revisar uso de path param versus query param na busca de livro por categoria (segmento de rota vazio retorna `404` em vez de alcançar o método).
- Implementar regra de bloqueio de exclusão de proprietário com livros associados (RN07), evitando erro `500` cru.
- Corrigir possível erro de digitação em `application.properties`
  (`%prod.quarkus.hernate-orm.database.generation`).
- Isolar o banco de dados usado pelos testes automatizados do banco usado manualmente em desenvolvimento.

### Entregáveis

- Código-fonte versionado no GitHub
- README com instruções de setup
- Documentação Swagger automática
- Testes com cobertura mínima de 70%
- API rodando em produção na VPS

---

## 🔄 Fase 2: Melhorias (v1.1.0)

**Status:** Planejado

**Objetivo:** Refinar funcionalidades, elevar o rigor das validações e adicionar recursos de produção.

### Funcionalidades Previstas

- [ ] Integrar validação algorítmica de CPF (verificação de dígitos verificadores via anotação `@CPF` ou integração com API externa de validação)
- [ ] Paginação nas listagens
- [ ] Transformar o campo categoria do livro em um Enum
- [ ] Melhor tratamento de exceções customizadas
- [ ] Testes de integração adicionais
- [ ] Flyway para versionamento do banco (incluindo separação de usuário de migração e usuário de runtime no Postgres)
- [ ] Revisar `quarkus.hibernate-orm.database.generation` em produção (trocar `update` por `validate`, com migrations explícitas via Flyway)
- [ ] Melhor estrutura de logs
- [ ] DTOs mais robustos

---

## 🔐 Fase 3: Segurança e Performance (v1.2.0)

**Status:** Planejado

**Objetivo:** Adicionar segurança e otimizações.

### Funcionalidades Previstas

- [ ] Autenticação com JWT
- [ ] Autorização por roles
- [ ] Cache estratégico
- [ ] Otimizações de query (índices, lazy loading)
- [ ] Monitoramento e métricas

---

## 🎯 Fase 4: Frontend (v2.0.0)

**Status:** Planejado

**Objetivo:** Interface web para consumir a API.

### Funcionalidades Previstas

- [ ] Frontend em React ou Vue.js
- [ ] Integração com API
- [ ] Interface responsiva
- [ ] Deploy junto com backend (reverse proxy Nginx servindo também os arquivos estáticos)

---

[Voltar ao README](../README.md)