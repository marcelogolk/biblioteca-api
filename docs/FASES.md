# Fases de Desenvolvimento

## Visao Geral

O projeto evolui de forma incremental, com releases versionadas. Cada fase consolida um conjunto de entregas antes de avancar para melhorias funcionais, seguranca e frontend.

## Fase 1: MVP (v1.0.0)

**Status: Concluida**

**Objetivo:** entregar o CRUD basico da Biblioteca API com arquitetura em camadas, regras essenciais de negocio, testes, documentacao e implantacao funcional em producao.

### Funcionalidades concluidas

- Estrutura do projeto Quarkus.
- Entidade `Proprietario`, com ID, nome e CPF como chave de negocio.
- Entidade `Livro`, com ID, titulo, autor, categoria, ano de publicacao e proprietario.
- Repositories com Panache.
- Services centralizando logica e regras de negocio.
- CPF imutavel depois do cadastro, com DTO especifico para atualizacao do proprietario.
- CRUD REST de Proprietario:
  - `POST /api/proprietarios`
  - `GET /api/proprietarios`
  - `GET /api/proprietarios/{id}`
  - `PUT /api/proprietarios/{id}`
  - `DELETE /api/proprietarios/{id}`
- CRUD REST de Livro:
  - `POST /api/livros`
  - `GET /api/livros`
  - `GET /api/livros/{id}`
  - `GET /api/livros/categoria/{categoria}`
  - `GET /api/livros/proprietario/{proprietarioId}`
  - `PUT /api/livros/{id}`
  - `DELETE /api/livros/{id}`
- Validacoes basicas com Bean Validation.
- Tratamento dos principais cenarios HTTP de negocio.
- Testes de entidade, service e integracao de controllers.
- Cobertura de testes superior ao objetivo inicial de 70%, com 93% registrado na conclusao do MVP.
- Swagger UI/OpenAPI.
- PostgreSQL em Docker Compose para desenvolvimento e producao.
- API Quarkus containerizada para arquitetura ARM64.
- Deploy concluido na Oracle Cloud VPS.
- PostgreSQL e API executando em containers na VPS.
- Persistencia validada apos reinicializacao dos containers.
- Nginx configurado como proxy reverso.
- Dominio e HTTPS configurados.
- API publicada em `https://api.marcelocarvalho.dev.br/api`.
- Porta da aplicacao restrita ao loopback da VPS por `127.0.0.1:8080:8080`.

### Arquitetura consolidada do MVP

```text
Cliente HTTP/HTTPS
       |
       v
Nginx (producao)
       |
       v
Controller -> Service -> Repository -> PostgreSQL
```

### Entregaveis da Fase 1

- Codigo-fonte versionado.
- README e documentacao tecnica do projeto.
- Swagger/OpenAPI.
- Suite automatizada de testes.
- Docker Compose para os ambientes local e VPS.
- API em execucao na Oracle Cloud VPS.
- Dominio proprio e acesso HTTPS.

### Encerramento da Fase 1

A Fase 1 e considerada concluida com o MVP funcional e implantado. Os requisitos que ainda precisam de refinamento ou implementacao deixam de bloquear a v1.0.0 e passam formalmente para a Fase 2.

---

## Fase 2: Melhorias funcionais, qualidade e producao (v1.1.0)

**Status: Em desenvolvimento**

**Objetivo:** resolver as dividas funcionais deixadas conscientemente fora do MVP, tornar os contratos e erros da API mais robustos e amadurecer persistencia, testes e operacao em producao.

### Requisitos funcionais transferidos da Fase 1

- Implementar a RN07: impedir a exclusao de proprietario que ainda possua livros associados, retornando resposta controlada em vez de erro interno.
- Revisar a busca de livros por categoria quanto ao uso de path parameter versus query parameter, incluindo o comportamento quando a categoria nao e informada.
- Isolar o banco utilizado pelos testes automatizados do banco utilizado manualmente no ambiente de desenvolvimento.

### Melhorias funcionais ja planejadas

- Integrar validacao algoritmica do CPF, alem da validacao sintatica atualmente existente.
- Implementar paginacao nas listagens.
- Transformar `categoria` de Livro em enum.
- Melhorar o tratamento de excecoes com respostas de erro padronizadas/customizadas.
- Garantir que payloads invalidos sejam rejeitados antes da persistencia, incluindo ausencia ou nome incorreto de `anoDePublicacao`.
- Tornar DTOs e contratos REST mais robustos.

### Persistencia e banco de dados

- Introduzir Flyway para versionamento do schema.
- Separar usuario de migracao e usuario de runtime no PostgreSQL.
- Trocar `%prod.quarkus.hibernate-orm.database.generation=update` por `validate` quando as migrations estiverem estabelecidas.
- Ampliar os testes de integracao relacionados a persistencia e regras de negocio.

### Operacao e observabilidade

- Melhorar a estrutura dos logs da aplicacao.
- Manter documentadas as configuracoes operacionais da VPS, Docker, Nginx e HTTPS.

### Resultado esperado

Ao final da Fase 2, a API deve manter as funcionalidades do MVP, mas com regras pendentes resolvidas, contratos HTTP mais previsiveis, banco versionado por migrations e uma separacao mais segura entre ambientes de desenvolvimento, teste e producao.

---

## Fase 3: Seguranca e Performance (v1.2.0)

**Status: Planejada**

**Objetivo:** adicionar autenticacao, autorizacao, observabilidade e otimizacoes apropriadas para a evolucao da API publica.

### Funcionalidades previstas

- Autenticacao com JWT.
- Autorizacao por roles.
- Cache estrategico.
- Otimizacoes de query, incluindo avaliacao de indices e carregamento de relacionamentos.
- Monitoramento e metricas.

---

## Fase 4: Frontend (v2.0.0)

**Status: Planejada**

**Objetivo:** criar uma interface web para consumir a Biblioteca API.

### Funcionalidades previstas

- Frontend em React ou Vue.js.
- Integracao com a API.
- Interface responsiva.
- Definicao de CORS de acordo com o cliente web adotado.
- Deploy integrado a infraestrutura existente, aproveitando o Nginx para o frontend e o proxy da API.

---

[Voltar ao README](../README.md)
