# 🏗️ Arquitetura

## Visão Geral

A aplicação segue uma arquitetura em camadas bem definida, separando responsabilidades e facilitando testes e manutenção.

---

## Diagrama de Arquitetura
┌─────────────────────────────────────────────────────────┐ │ Cliente REST │ │ (Postman, Frontend, etc) │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ CONTROLLER LAYER │ │ (ProprietarioController, LivroController) │ │ - Recebe requisições HTTP │ │ - Valida entrada (DTOs) │ │ - Retorna respostas HTTP │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ SERVICE LAYER │ │ (ProprietarioService, LivroService) │ │ - Lógica de negócio │ │ - Validações de regras │ │ - Orquestração de operações │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ REPOSITORY LAYER │ │ (ProprietarioRepository, LivroRepository) │ │ - Acesso a dados │ │ - Queries customizadas │ │ - Operações CRUD │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ ENTITY LAYER │ │ (Proprietario, Livro) │ │ - Mapeamento JPA │ │ - Relacionamentos │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ DATABASE LAYER │ │ (PostgreSQL) │ └─────────────────────────────────────────────────────────┘

---

## Estrutura de Pastas
src/main/java/br/dev/marcelocarvalho/ ├── controller/ │ ├── ProprietarioController.java │ └── LivroController.java ├── service/ │ ├── ProprietarioService.java │ └── LivroService.java ├── repository/ │ ├── ProprietarioRepository.java │ └── LivroRepository.java ├── entity/ │ ├── Proprietario.java │ └── Livro.java └── dto/ ├── ProprietarioDTO.java └── LivroDTO.java

src/main/resources/ ├── application.properties └── import.sql (dados iniciais)

src/test/java/br/dev/marcelocarvalho/ ├── service/ │ ├── ProprietarioServiceTest.java │ └── LivroServiceTest.java └── controller/ ├── ProprietarioControllerTest.java └── LivroControllerTest.java

---

## Responsabilidades por Camada

### Controller

- Receber requisições HTTP
- Validar DTOs de entrada
- Chamar serviços
- Retornar respostas HTTP apropriadas
- Mapear exceções para códigos HTTP

### Service

- Implementar lógica de negócio
- Validar regras de negócio
- Orquestrar operações entre repositories
- Lançar exceções customizadas

### Repository

- Implementar operações CRUD
- Executar queries customizadas
- Gerenciar transações
- Retornar entidades

### Entity

- Representar tabelas do banco
- Definir relacionamentos
- Incluir validações JPA

### DTO

- Transferir dados entre camadas
- Desacoplar API interna de externa
- Validar entrada/saída

---

## Fluxo de uma Requisição
Cliente envia POST /api/livros com JSON ↓
Controller recebe e converte para LivroDTO ↓
Controller chama LivroService.criar(livroDTO) ↓
Service valida regras de negócio ↓
Service chama LivroRepository.persist(livro) ↓
Repository persiste no PostgreSQL ↓
Service retorna livro criado ↓
Controller retorna HTTP 201 com JSON do livro ↓
Cliente recebe resposta
---

## Padrões Utilizados

- **Layered Architecture** — Separação de responsabilidades
- **Repository Pattern** — Abstração de acesso a dados
- **Service Layer** — Lógica de negócio centralizada
- **DTO Pattern** — Transferência de dados
- **Dependency Injection** — Quarkus CDI

---

[Voltar ao README](../README.md)