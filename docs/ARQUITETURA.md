# Arquitetura da Biblioteca API

## 1. Visão geral

A Biblioteca API utiliza uma arquitetura em camadas no código Java e uma arquitetura containerizada na infraestrutura de produção.

As duas visões são complementares:

- a **arquitetura da aplicação** descreve a organização do código e o fluxo das regras de negócio;
- a **arquitetura de infraestrutura** descreve como clientes externos, Nginx, API, Docker e PostgreSQL se conectam na Oracle Cloud VPS.

A versão 1.0.0 corresponde ao MVP funcional, testado e publicado em produção.

---

## 2. Arquitetura da aplicação

### 2.1 Fluxo principal

```text
Cliente REST
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Entidades JPA
    |
    v
PostgreSQL
```

### 2.2 Controller

Principais componentes:

- `ProprietarioController`
- `LivroController`

Responsabilidades:

- receber requisições HTTP;
- interpretar path parameters e payloads JSON;
- aplicar validação dos DTOs de entrada;
- delegar operações à camada Service;
- devolver respostas HTTP e DTOs de saída.

A camada Controller não deve concentrar regras dependentes do estado do banco.

### 2.3 Service

Principais componentes:

- `ProprietarioService`
- `LivroService`

Responsabilidades:

- executar a lógica de negócio;
- coordenar repositories;
- verificar regras que dependem do banco;
- controlar operações transacionais;
- converter entidades para DTOs de resposta;
- sinalizar cenários como recurso inexistente ou conflito de negócio.

No MVP, os Services ainda utilizam exceções HTTP em parte dos fluxos. A adoção de exceções próprias e respostas de erro padronizadas está planejada para a Fase 2.

### 2.4 Repository

Principais componentes:

- `ProprietarioRepository`
- `LivroRepository`

Responsabilidades:

- encapsular acesso ao banco com Hibernate ORM e Panache;
- executar operações CRUD;
- executar consultas por categoria e proprietário diretamente no banco;
- retornar entidades ou resultados de consulta à camada Service.

Os repositories não são o local principal de definição das transações de negócio. A delimitação transacional pertence aos fluxos coordenados pelos Services.

### 2.5 Entity

Principais componentes:

- `Proprietario`
- `Livro`
- `YearAttributeConverter`

Responsabilidades:

- mapear as tabelas e colunas do banco;
- representar o relacionamento entre Livro e Proprietário;
- definir restrições de persistência;
- preservar as chaves de negócio das entidades;
- converter `java.time.Year` para o tipo persistido no PostgreSQL.

Identidades adotadas:

- Proprietário: CPF como chave de negócio imutável;
- Livro: UUID como chave de negócio imutável;
- IDs numéricos: chaves técnicas geradas para persistência.

### 2.6 DTO

Contratos existentes:

- `ProprietarioDTO`
- `ProprietarioInclusaoDTO`
- `ProprietarioAtualizacaoDTO`
- `LivroDTO`
- `LivroInclusaoDTO`
- `LivroAtualizacaoDTO`

Responsabilidades:

- definir o contrato público da API;
- impedir exposição direta das entidades JPA;
- validar campos de entrada;
- separar inclusão e atualização;
- tornar estruturais regras como CPF imutável e proprietário do Livro imutável.

Exemplos:

- `ProprietarioAtualizacaoDTO` não contém CPF;
- `LivroAtualizacaoDTO` não contém `proprietarioId`.

---

## 3. Fluxos da aplicação

### 3.1 Cadastro de Livro

```text
POST /api/livros
    |
    v
LivroController recebe LivroInclusaoDTO
    |
    v
Bean Validation valida o payload
    |
    v
LivroService verifica o proprietário
    |
    v
LivroRepository persiste o Livro
    |
    v
LivroService retorna LivroDTO
    |
    v
Controller retorna HTTP 201
```

O cliente informa apenas `proprietarioId`. A camada Service busca o Proprietário gerenciado pelo Hibernate antes de criar o Livro.

### 3.2 Busca de livros por proprietário

```text
GET /api/livros/proprietario/{proprietarioId}
    |
    v
LivroController recebe o identificador
    |
    v
LivroService delega a consulta
    |
    v
LivroRepository filtra no PostgreSQL
    |
    v
Service converte o resultado em LivroDTO
    |
    v
Controller retorna HTTP 200
```

Uma consulta sem resultados pode retornar lista vazia. O mesmo princípio de filtro no banco é usado na busca por categoria.

### 3.3 Atualizações com contratos restritos

```text
PUT /api/proprietarios/{id}
    -> permite atualizar o nome
    -> não permite alterar o CPF

PUT /api/livros/{id}
    -> permite atualizar dados editáveis do Livro
    -> não permite alterar o proprietário
```

A restrição é garantida pelo formato dos DTOs de atualização, reduzindo a dependência de validações condicionais no Service.

---

## 4. Arquitetura de infraestrutura em produção

### 4.1 Visão externa

```text
Cliente externo
    |
    | HTTPS :443
    v
api.marcelocarvalho.dev.br
    |
    v
Nginx na Oracle Cloud VPS
    |
    | proxy reverso para 127.0.0.1:8080
    v
Container biblioteca-api
    |
    | rede interna do Docker Compose
    v
Container PostgreSQL
    |
    v
Volume Docker biblioteca_api_data
```

A API está publicada em:

```text
https://api.marcelocarvalho.dev.br/api
```

Não existe frontend nesta versão. O domínio raiz permanece disponível para uma interface futura.

### 4.2 Oracle Cloud VPS

Características registradas para o ambiente:

- Oracle Cloud Infrastructure;
- região de São Paulo;
- shape ARM64/Ampere;
- Ubuntu 24.04 Minimal;
- acesso administrativo por SSH na porta 51022;
- Docker e Docker Compose instalados no host.

A imagem da Biblioteca API é compatível com ARM64 e utiliza Java 21 no runtime.

### 4.3 Nginx

O Nginx é executado no host da VPS e atua como borda pública da aplicação.

Responsabilidades:

- receber tráfego HTTP e HTTPS;
- redirecionar HTTP para HTTPS;
- terminar a conexão TLS;
- encaminhar requisições ao Quarkus;
- manter logs de acesso e erro;
- preservar o domínio raiz para o frontend futuro.

O tráfego público não se conecta diretamente ao container da aplicação.

### 4.4 API em container

O serviço `biblioteca-api`:

- utiliza a imagem `biblioteca-api:arm64`;
- executa com o perfil `prod` do Quarkus;
- recebe as credenciais do banco por variáveis de ambiente;
- depende do healthcheck do PostgreSQL;
- publica a porta no loopback do host:

```yaml
ports:
  - "127.0.0.1:8080:8080"
```

Esse binding permite que o Nginx acesse a aplicação no próprio host e impede que a porta 8080 seja publicada em todas as interfaces de rede.

### 4.5 PostgreSQL em container

O serviço `postgres`:

- utiliza PostgreSQL 16.4;
- não publica a porta 5432 na VPS;
- recebe configurações pelo arquivo `.env`;
- possui healthcheck com `pg_isready`;
- persiste dados no volume nomeado `biblioteca_api_data`;
- é acessado pela API usando o nome do serviço `postgres` na rede do Compose.

URL JDBC do perfil de produção:

```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/biblioteca_api
```

### 4.6 Rede e exposição

| Componente | Porta | Exposição |
|---|---:|---|
| SSH | 51022 | Pública, com autenticação por chave |
| Nginx HTTP | 80 | Pública, usada para redirecionamento/validação |
| Nginx HTTPS | 443 | Pública |
| Quarkus | 8080 | Somente loopback `127.0.0.1` |
| PostgreSQL | 5432 | Somente rede interna do Docker Compose |

A exposição é controlada pelas regras de rede da Oracle Cloud e pelas camadas de firewall do host.

### 4.7 Persistência e inicialização

```text
Docker Compose inicia PostgreSQL
    |
    v
Healthcheck confirma banco saudável
    |
    v
Docker Compose inicia Biblioteca API
    |
    v
Quarkus conecta em postgres:5432
```

O volume do PostgreSQL é preservado durante reinicializações normais. A remoção do volume com `docker compose down -v` apaga os dados e não faz parte do procedimento normal de produção.

---

## 5. Ambientes

### 5.1 Desenvolvimento local

```text
Quarkus Dev no host
    |
    v
localhost:5432
    |
    v
PostgreSQL no Docker Compose local
```

Características:

- API iniciada por Maven/Quarkus Dev;
- PostgreSQL em container;
- porta 5432 publicada no host;
- variáveis de ambiente carregadas antes da execução do Maven.

### 5.2 Produção 

```text
Internet
    |
    v
Nginx + HTTPS
    |
    v
Quarkus em container
    |
    v
PostgreSQL em container
```

Características:

- API e banco gerenciados pelo Docker Compose;
- Nginx no host;
- persistência em volume Docker;
- perfil `prod` do Quarkus;
- domínio próprio e HTTPS.
- SQL logging do Hibernate desabilitado no perfil de produção.

### 5.3 Testes automatizados

A suíte contém testes de:

- entidades;
- conversor de `Year`;
- Services com mocks;
- integração dos Controllers;
- validações e principais cenários HTTP.

O projeto registrou cobertura de 93% ao encerrar o MVP. O isolamento do banco de testes em relação ao banco usado manualmente em desenvolvimento está planejado para a Fase 2.

---

## 6. Estrutura principal do projeto

```text
biblioteca-api/
├── docs/
│   ├── ARQUITETURA.md
│   ├── COMO_RODAR.md
│   ├── DEPLOY_VPS_BIBLIOTECA_API.md
│   ├── DIAGRAMAS.md
│   ├── FASES.md
│   ├── REGRAS_NEGOCIO.md
│   └── TECNOLOGIAS.md
├── http/
│   └── biblioteca.http
├── local/
│   ├── docker-compose.yml
│   └── init/
│       └── 01-create-app-user.sh
├── vps/
│   ├── docker-compose.yml
│   └── init/
│       └── 01-create-app-user.sh
├── src/
│   ├── main/
│   │   ├── docker/
│   │   ├── java/br/dev/marcelocarvalho/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── import.sql
│   └── test/java/br/dev/marcelocarvalho/
│       ├── controller/
│       ├── entity/
│       └── service/
├── HISTORY.md
├── pom.xml
└── README.md
```

Relatórios de cobertura e artefatos de build não fazem parte da arquitetura lógica da aplicação, mesmo que possam existir no diretório de trabalho.

---

## 7. Padrões e decisões arquiteturais

- **Layered Architecture:** separação entre Controller, Service e Repository.
- **Repository Pattern:** acesso a dados encapsulado.
- **Service Layer:** coordenação de regras e transações de negócio.
- **DTO Pattern:** contratos REST separados das entidades.
- **Dependency Injection:** beans gerenciados pelo Quarkus CDI.
- **Bean Validation:** validação declarativa de entradas.
- **Business Key:** CPF para Proprietário e UUID para Livro.
- **Database per application:** PostgreSQL dedicado à Biblioteca API.
- **Reverse Proxy:** Nginx como borda pública e terminação TLS.
- **Containerização:** aplicação e banco isolados em containers.
- **Externalized Configuration:** credenciais fornecidas por variáveis de ambiente e arquivos `.env` não versionados.
- **Persistent Volume:** dados do PostgreSQL fora do ciclo de vida do container.

---

## 8. Limites atuais e evolução planejada

### Fase 2

- paginação;
- bloqueio de exclusão de proprietário com livros;
- categoria como enum;
- exceções e erros padronizados;
- Flyway;
- `database.generation=validate` em produção;
- usuários separados para migração e runtime;
- banco de testes isolado;
- melhoria da estrutura e da observabilidade dos logs da aplicação.

### Fase 3

- autenticação JWT;
- autorização por roles;
- monitoramento e métricas;
- cache e otimizações.

### Fase 4

- frontend web;
- CORS definido conforme a origem do frontend;
- Nginx servindo arquivos estáticos e mantendo o proxy da API.

---

## 9. Documentos relacionados

- [Regras de negócio](REGRAS_NEGOCIO.md)
- [Fases do projeto](FASES.md)
- [Tecnologias utilizadas](TECNOLOGIAS.md)
- [Execução local](COMO_RODAR.md)
- [Deploy na Oracle Cloud VPS](DEPLOY_VPS_BIBLIOTECA_API.md)
- [Histórico de desenvolvimento](../HISTORY.md)

[Voltar ao README](../README.md)
