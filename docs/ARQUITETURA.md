# 🏗️ Arquitetura

## Visão Geral

A aplicação segue uma arquitetura em camadas bem definida, separando responsabilidades e facilitando testes e manutenção.

---

## Diagrama de Arquitetura
```
┌──────────────────────────────────────────────────────────┐
│                  Cliente REST                            │
│              (Postman, Frontend, etc)                    │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌───────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                        │
│         (ProprietarioController, LivroController)         │
│  - Recebe requisições HTTP                                │
│  - Valida entrada (DTOs)                                  │
│  - Retorna respostas HTTP                                 │
└────────────────────┬──────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                           │
│            (ProprietarioService, LivroService)             │
│  - Lógica de negócio                                       │
│  - Validações de regras                                    │
│  - Orquestração de operações                               │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                          │
│       (ProprietarioRepository, LivroRepository)            │
│  - Acesso a dados                                          │
│  - Queries customizadas (filtros delegados ao banco)       │
│  - Operações CRUD                                          │
└────────────────────┬───────────────────────────────────────┘
                     │     
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    ENTITY LAYER                             │
│                (Proprietario, Livro)                        │
│  - Mapeamento JPA                                           │
│  - Relacionamentos                                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   DATABASE LAYER                            │
│                    (PostgreSQL)                             │
└─────────────────────────────────────────────────────────────┘
```
---

## Estrutura de Pastas
```
src/main/java/br/dev/marcelocarvalho/
├── controller/
│   ├── ProprietarioController.java
│   └── LivroController.java
├── service/
│   ├── ProprietarioService.java
│   └── LivroService.java
├── repository/
│   ├── ProprietarioRepository.java
│   └── LivroRepository.java
├── entity/
│   ├── Proprietario.java
│   ├── Livro.java
│   └── converter/
│       └── YearAttributeConverter.java
└── dto/
    ├── ProprietarioDTO.java
    ├── ProprietarioInclusaoDTO.java
    ├── ProprietarioAtualizacaoDTO.java
    ├── LivroDTO.java
    ├── LivroInclusaoDTO.java
    └── LivroAtualizacaoDTO.java

src/main/resources/
├── application.properties
└── import.sql (dados iniciais)

src/test/java/br/dev/marcelocarvalho/
├── entity/
│   ├── ProprietarioTest.java
│   ├── LivroTest.java
│   └── converter/
│       └── YearAttributeConverterTest.java
├── service/
│   └── LivroServiceTest.java        (estrutura criada, implementação pendente)
└── controller/
    ├── ProprietarioControllerTest.java  (estrutura criada, implementação pendente)
    └── LivroControllerTest.java
```
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
- Executar queries customizadas (incluindo filtros por categoria e por proprietário, resolvidos diretamente no banco)
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
- Separar contratos de entrada por operação (inclusão vs. atualização), restringindo estruturalmente quais campos cada operação aceita


---

## Fluxo de uma Requisição
```
Cliente envia POST /api/livros com JSON
        ↓
Controller recebe e converte para LivroInclusaoDTO
        ↓
Controller chama LivroService.incluirLivro(dto)
        ↓
Service valida regras de negócio
        ↓
Service busca o Proprietario existente via ProprietarioRepository
        ↓
Service chama LivroRepository.persist(livro)
        ↓
Repository persiste no PostgreSQL
        ↓
Service retorna LivroDTO criado
        ↓
Controller retorna HTTP 201 com JSON do livro
        ↓
Cliente recebe resposta
```
## Fluxo de uma Requisição
```
Cliente envia GET /api/livros/proprietario/{proprietarioId}
        ↓
Controller recebe o parâmetro de rota
        ↓
Controller chama LivroService.buscarLivroByProprietarioId(id)
        ↓
Service chama LivroRepository.findByProprietarioId(id)
        ↓
Repository executa a consulta filtrada diretamente no banco
        ↓
Service converte os resultados em List<LivroDTO>
        ↓
Controller retorna HTTP 200 com a lista (vazia ou não)
        ↓
Cliente recebe resposta
```
 - O mesmo padrão se aplica à busca por categoria (GET /api/livros/categoria/{categoria}).
---

## Padrões Utilizados

- **Layered Architecture** — Separação de responsabilidades
- **Repository Pattern** — Abstração de acesso a dados
- **Service Layer** — Lógica de negócio centralizada
- **DTO Pattern** — Transferência de dados, com contratos de entrada separados por operação
- **Dependency Injection** — Quarkus CDI
- **Business Key** — Identidade de entidade JPA baseada em chave de negócio imutável (cpf em Proprietario, uuid em Livro), em vez do identificador técnico gerado pelo banco

---

[Voltar ao README](../README.md)