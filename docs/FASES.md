# 📅 Fases de Desenvolvimento

## Visão Geral

O projeto será desenvolvido de forma incremental, com releases versionadas. Cada fase adiciona funcionalidades e melhora a qualidade do código.

---

## ✅ Fase 1: MVP (v1.0.0)

**Status:** Em desenvolvimento

**Objetivo:** Implementar o CRUD básico com arquitetura em camadas.

### Funcionalidades

- [x] Estrutura do projeto Quarkus
- [ ] Entidade `Proprietario` (Id, Nome)
- [ ] Entidade `Livro` (Id, Título, Autor, Categoria, Ano, Proprietário)
- [ ] Repository com Panache para ambas as entidades
- [ ] Service com lógica de negócio
- [ ] Controller REST com endpoints:
    - `POST /api/proprietarios` — Criar proprietário
    - `GET /api/proprietarios` — Listar todos
    - `GET /api/proprietarios/{id}` — Buscar por ID
    - `PUT /api/proprietarios/{id}` — Atualizar
    - `DELETE /api/proprietarios/{id}` — Deletar
    - `POST /api/livros` — Criar livro
    - `GET /api/livros` — Listar todos
    - `GET /api/livros/{id}` — Buscar por ID
    - `GET /api/livros/categoria/{categoria}` — Buscar por categoria
    - `GET /api/livros/proprietario/{proprietarioId}` — Buscar por proprietário
    - `PUT /api/livros/{id}` — Atualizar
    - `DELETE /api/livros/{id}` — Deletar
- [ ] Validações de negócio
- [ ] Tratamento de erros (400, 404, 500)
- [ ] Testes unitários (70% cobertura)
- [ ] Documentação Swagger UI
- [ ] Deploy em VPS Oracle Cloud

### Arquitetura
controller/ → service/ → repository/ → banco de dados

### Entregáveis

- Código-fonte versionado no GitHub
- README com instruções de setup
- Documentação Swagger automática
- Testes com cobertura mínima de 70%
- API rodando em produção na VPS

---

## 🔄 Fase 2: Melhorias (v1.1.0)

**Status:** Planejado

**Objetivo:** Refinar funcionalidades e adicionar recursos de produção.

### Funcionalidades Previstas

- [ ] Paginação nas listagens
- [ ] Melhor tratamento de exceções customizadas
- [ ] Testes de integração
- [ ] Flyway para versionamento do banco
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
- [ ] Deploy junto com backend

---

[Voltar ao README](../README.md)