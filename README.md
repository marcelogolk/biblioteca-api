# Biblioteca API

API REST para gerenciamento de uma biblioteca particular, desenvolvida com Java 21, Quarkus, Hibernate ORM com Panache e PostgreSQL.

O projeto nasceu como exercício prático de desenvolvimento backend e evoluiu até um MVP funcional em produção, cobrindo modelagem de domínio, regras de negócio, testes automatizados, persistência, containerização e publicação em uma VPS Oracle Cloud.

## Objetivo

Organizar e gerenciar livros de uma biblioteca particular, permitindo cadastrar proprietários e livros e executar consultas, atualizações e exclusões por meio de uma API REST.

O projeto também serve como ambiente de estudo e evolução incremental de Java, Quarkus, APIs REST, PostgreSQL, Docker e infraestrutura Linux.

---

## Estado atual

**MVP v1.0.0 concluído.**

A aplicação está em execução na Oracle Cloud VPS, com:

- API Quarkus em container Docker;
- PostgreSQL 16.4 em container Docker;
- persistência em volume Docker;
- Nginx como proxy reverso;
- HTTPS configurado;
- domínio próprio;
- porta 8080 da aplicação restrita ao loopback da VPS;
- testes automatizados com 93% de cobertura registrada ao final do MVP.

### API pública

```text
https://api.marcelocarvalho.dev.br/api
```

### Domínio registrado

```text
marcelocarvalho.dev.br
```

O subdomínio `api.marcelocarvalho.dev.br` é utilizado pela API. O domínio raiz permanece disponível para evolução futura do projeto, como a inclusão de um frontend.

---

## Atenção: API pública nesta fase

Nesta etapa do projeto, a API **ainda não possui autenticação nem autorização**.

Isso significa que os endpoints publicados podem ser acessados sem login ou senha e que operações de escrita disponíveis pela API também estão abertas enquanto o MVP permanecer nessa configuração.

Por esse motivo:

- a instância pública deve ser utilizada somente com dados fictícios ou de teste;
- não devem ser cadastrados dados pessoais, confidenciais ou reais;
- os registros atuais podem ser alterados ou excluídos por terceiros;
- a API pública não deve ser tratada como ambiente adequado para armazenar informações importantes.

Essa exposição é uma decisão temporária desta fase de aprendizado. Segurança da aplicação está prevista para uma fase posterior, quando serão definidos autenticação, autorização e o mecanismo de acesso dos usuários.

---

## Stack atual

| Área | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Quarkus Platform 3.39.3 |
| REST/JSON | Quarkus REST + Jackson |
| Persistência | Hibernate ORM + Panache |
| Validação | Hibernate Validator / Bean Validation |
| Banco | PostgreSQL 16.4 |
| Build | Maven |
| Testes | Quarkus JUnit, Mockito, REST Assured e JaCoCo |
| Documentação da API | SmallRye OpenAPI / Swagger UI |
| Containers | Docker + Docker Compose |
| Proxy reverso | Nginx |
| Servidor | Oracle Cloud VPS ARM64/Ampere |
| Sistema operacional da VPS | Ubuntu 24.04 Minimal ARM64 |
| Transporte público | HTTPS |

As versões e dependências conhecidas são detalhadas em [Tecnologias Utilizadas](docs/TECNOLOGIAS.md).

---

## Arquitetura resumida

### Aplicação

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Hibernate ORM / Panache
    |
    v
PostgreSQL
```

### Produção

```text
Cliente externo
    |
    | HTTPS :443
    v
api.marcelocarvalho.dev.br
    |
    v
Nginx
    |
    | proxy reverso
    v
127.0.0.1:8080
    |
    v
Container Biblioteca API
    |
    | rede interna Docker
    v
Container PostgreSQL
    |
    v
Volume persistente
```

A porta 8080 não é publicada em todas as interfaces do host. O Compose da VPS utiliza:

```yaml
ports:
  - "127.0.0.1:8080:8080"
```

Mais detalhes estão em [Arquitetura](docs/ARQUITETURA.md) e [Deploy na VPS](docs/DEPLOY_VPS_BIBLIOTECA_API.md).

---

## Principais funcionalidades do MVP

### Proprietários

- cadastrar proprietário;
- listar proprietários;
- buscar proprietário por ID;
- atualizar proprietário;
- excluir proprietário;
- CPF utilizado como chave de negócio e mantido imutável após o cadastro.

### Livros

- cadastrar livro;
- listar livros;
- buscar livro por ID;
- buscar livros por categoria;
- buscar livros por proprietário;
- atualizar livro;
- excluir livro;
- UUID utilizado como chave de negócio;
- proprietário do livro não pode ser alterado depois da criação.

As regras completas e o estado de implementação estão documentados em [Regras de Negócio](docs/REGRAS_NEGOCIO.md).

---

## Endpoints principais

### Proprietários

```text
POST   /api/proprietarios
GET    /api/proprietarios
GET    /api/proprietarios/{id}
PUT    /api/proprietarios/{id}
DELETE /api/proprietarios/{id}
```

### Livros

```text
POST   /api/livros
GET    /api/livros
GET    /api/livros/{id}
GET    /api/livros/categoria/{categoria}
GET    /api/livros/proprietario/{proprietarioId}
PUT    /api/livros/{id}
DELETE /api/livros/{id}
```

Na instância pública, os caminhos acima são acessados a partir de:

```text
https://api.marcelocarvalho.dev.br
```

Exemplo:

```text
GET https://api.marcelocarvalho.dev.br/api/livros
```

---

## Execução local

A configuração local mantém a API executada pelo Quarkus no host e o PostgreSQL em Docker.

### Pré-requisitos

- Java 21;
- Maven ou Maven Wrapper do projeto;
- Docker;
- Docker Compose.

Os comandos e variáveis necessários estão documentados em [Como Rodar Localmente](docs/COMO_RODAR.md).

Execução da aplicação em modo de desenvolvimento:

```bash
./mvnw quarkus:dev
```

Quando executada localmente, a API utiliza:

```text
http://localhost:8080
```

---

## Swagger / OpenAPI

A API utiliza SmallRye OpenAPI e Swagger UI para documentação interativa.

No ambiente local de desenvolvimento:

```text
http://localhost:8080/q/swagger-ui/
```

A disponibilidade do Swagger no ambiente público depende da configuração do perfil de produção e não deve ser presumida a partir da configuração de desenvolvimento.

---

## Testes

O projeto possui testes de:

- entidades;
- conversão de `Year`;
- Services com mocks;
- Controllers;
- validações;
- operações REST e cenários HTTP.

A cobertura registrada na conclusão da Fase 1 é de **93%**.

O isolamento completo do banco usado pelos testes automatizados está planejado para a Fase 2.

---

## Fases do projeto

### Fase 1 - MVP v1.0.0

**Concluída.**

Inclui domínio inicial, CRUDs, testes, documentação, PostgreSQL, containerização, deploy em VPS, domínio, Nginx e HTTPS.

### Fase 2 - Melhorias funcionais e qualidade v1.1.0

Planejada para incluir, entre outros pontos:

- paginação;
- validação algorítmica de CPF;
- categoria como enum;
- bloqueio controlado da exclusão de proprietário com livros associados;
- exceções e respostas de erro padronizadas;
- Flyway;
- isolamento do banco de testes;
- revisão dos logs de produção.

### Fase 3 - Segurança e performance v1.2.0

Planejada para incluir:

- autenticação;
- autorização por roles;
- definição do mecanismo de login e acesso dos usuários;
- monitoramento e métricas;
- cache e otimizações quando justificadas.

### Fase 4 - Frontend v2.0.0

Planejada para adicionar uma interface web e integrá-la à infraestrutura existente.

O planejamento detalhado está em [Fases de Desenvolvimento](docs/FASES.md).

---

## Documentação

- [Tecnologias Utilizadas](docs/TECNOLOGIAS.md)
- [Fases de Desenvolvimento](docs/FASES.md)
- [Regras de Negócio](docs/REGRAS_NEGOCIO.md)
- [Como Rodar Localmente](docs/COMO_RODAR.md)
- [Arquitetura](docs/ARQUITETURA.md)
- [Deploy da Biblioteca API - Oracle Cloud VPS](docs/DEPLOY_VPS_BIBLIOTECA_API.md)
- [Diagramas](docs/DIAGRAMAS.md)
- [Histórico de Desenvolvimento](HISTORY.md)

---

## Próximo marco

Com o MVP publicado e a infraestrutura básica concluída, o foco passa para a Fase 2: refinamento das regras pendentes, evolução dos contratos REST, paginação, migrations de banco e melhoria do tratamento de erros.

A segurança de acesso permanece planejada para a Fase 3. Até sua implementação, a API pública continuará sendo tratada exclusivamente como ambiente de estudo e testes com dados fictícios.

---

## Feedback e sugestões

Sugestões e problemas podem ser enviados para:

```text
biblioteca.marcelo.g.olk@gmail.com
```

---

## Licença

Projeto de código aberto desenvolvido para fins educacionais e para estudo de desenvolvimento backend com Java.
