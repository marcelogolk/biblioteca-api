# Tecnologias Utilizadas

## 1. Objetivo

Este documento registra as tecnologias efetivamente utilizadas pela Biblioteca API no encerramento do MVP v1.0.0.

Para manter valor histórico e permitir reprodução do ambiente, versões conhecidas são registradas explicitamente. Quando a versão exata de uma ferramenta não está registrada nos artefatos atuais do projeto, o documento informa isso em vez de presumir uma versão.

---

## 2. Backend

| Tecnologia | Versão registrada | Uso no projeto |
|---|---:|---|
| Java | 21 | Linguagem e runtime da aplicação |
| Quarkus Platform | 3.39.3 | Plataforma principal do backend |
| Quarkus REST | gerenciada pelo BOM 3.39.3 | Implementação dos endpoints REST |
| Quarkus REST Jackson | gerenciada pelo BOM 3.39.3 | Serialização e desserialização JSON |
| Hibernate ORM | gerenciada pelo BOM 3.39.3 | Persistência JPA |
| Hibernate ORM with Panache | gerenciada pelo BOM 3.39.3 | Implementação dos repositories |
| Hibernate Validator | gerenciada pelo BOM 3.39.3 | Bean Validation dos DTOs |
| Quarkus Arc | gerenciada pelo BOM 3.39.3 | Injeção de dependências/CDI |
| PostgreSQL JDBC | gerenciada pelo BOM 3.39.3 | Conectividade entre a aplicação e PostgreSQL |
| SmallRye OpenAPI | gerenciada pelo BOM 3.39.3 | Especificação OpenAPI e Swagger UI |

### Observação sobre as versões Quarkus

As extensões Quarkus não possuem versões individuais fixadas no `pom.xml`. Elas herdam versões compatíveis do Quarkus BOM `3.39.3`. Por isso, a documentação registra explicitamente a versão do BOM em vez de presumir versões individuais.

---

## 3. Banco de dados

| Tecnologia | Versão registrada | Uso no projeto |
|---|---:|---|
| PostgreSQL | 16.4 | Banco de dados relacional em desenvolvimento e produção |

Em produção, PostgreSQL executa em container Docker e utiliza volume nomeado para persistência. A porta 5432 não é publicada externamente na VPS.

A aplicação em produção acessa o banco pelo nome do serviço Docker:

```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/biblioteca_api
```

O gerenciamento de schema ainda utiliza `database.generation=update`. A introdução do Flyway e a alteração para `validate` estão planejadas para a Fase 2.

---

## 4. Build e gerenciamento de dependências

| Tecnologia | Versão registrada | Uso no projeto |
|---|---:|---|
| Maven | versão exata não registrada neste documento | Build, dependências e ciclo de testes |
| Maven Compiler Plugin | 3.15.0 | Compilação Java |
| Maven Surefire Plugin | 3.5.6 | Execução da suíte de testes |
| Quarkus Maven Plugin | 3.39.3 | Build e integração do Quarkus com Maven |

O projeto é compilado para Java 21.

Artefato do projeto:

```text
br.dev.marcelocarvalho:biblioteca-api:1.0.0-SNAPSHOT
```

---

## 5. Dependências principais

As dependências atuais declaradas no `pom.xml` incluem:

### Runtime

```text
io.quarkus:quarkus-smallrye-openapi
io.quarkus:quarkus-hibernate-orm-panache
io.quarkus:quarkus-jdbc-postgresql
io.quarkus:quarkus-arc
io.quarkus:quarkus-hibernate-orm
io.quarkus:quarkus-hibernate-validator
io.quarkus:quarkus-rest
io.quarkus:quarkus-rest-jackson
```

### Testes

```text
io.quarkus:quarkus-junit
io.quarkus:quarkus-junit-mockito
io.quarkus:quarkus-jacoco
io.rest-assured:rest-assured
```

A API utiliza a pilha atual `quarkus-rest` / `quarkus-rest-jackson`. As antigas referências documentais a RESTEasy Classic não representam mais a configuração atual do projeto.

---

## 6. Testes e qualidade

| Tecnologia | Versão | Uso no projeto |
|---|---:|---|
| Quarkus JUnit | gerenciada pelo BOM 3.39.3 | Testes integrados ao runtime Quarkus |
| Quarkus JUnit Mockito | gerenciada pelo BOM 3.39.3 | Mocks em testes de Service |
| JaCoCo | gerenciada pela plataforma Quarkus | Cobertura de testes |
| REST Assured | gerenciada pelas dependências do projeto | Testes dos endpoints HTTP |

No encerramento do MVP, a documentação do projeto registra cobertura de testes de 93%.

A suíte cobre entidades, conversão de `Year`, Services e Controllers. O isolamento do banco de testes em relação ao banco utilizado manualmente em desenvolvimento permanece planejado para a Fase 2.

---

## 7. API e documentação

| Tecnologia | Versão registrada | Uso no projeto |
|---|---:|---|
| SmallRye OpenAPI | gerenciada pelo BOM 3.39.3 | Geração da especificação OpenAPI |
| Swagger UI | fornecida pela integração OpenAPI do Quarkus | Interface de documentação e exploração da API |

O caminho base da aplicação é `/api`.

A documentação técnica do projeto é mantida em Markdown no diretório `docs/` e complementada pelo `HISTORY.md`.

---

## 8. Containerização

| Tecnologia | Versão registrada | Uso no projeto |
|---|---:|---|
| Docker Engine | versão exata não registrada na documentação atual | Execução dos containers |
| Docker Compose Plugin | versão exata não registrada na documentação atual | Orquestração local e na VPS |
| PostgreSQL Docker Image | 16.4 | Banco containerizado |
| Biblioteca API Image | `biblioteca-api:arm64` | Aplicação Quarkus em produção |

Na VPS, Docker Compose gerencia dois serviços principais:

- `postgres`;
- `biblioteca-api`.

O serviço da API depende do healthcheck do PostgreSQL.

A publicação da aplicação no host está restrita ao loopback:

```yaml
ports:
  - "127.0.0.1:8080:8080"
```

A imagem da aplicação é construída para a arquitetura ARM64 da VPS e executa Java 21.

---

## 9. Infraestrutura de produção

| Tecnologia/serviço | Versão ou configuração registrada | Uso |
|---|---|---|
| Oracle Cloud Infrastructure | região São Paulo | Hospedagem da VPS |
| VM.Standard.A1.Flex | ARM64/Ampere | Shape da VPS |
| Ubuntu | 24.04 Minimal ARM64 | Sistema operacional da VPS |
| Nginx | versão exata não registrada na documentação atual | Proxy reverso e terminação HTTPS |
| HTTPS/TLS | certificado válido configurado | Exposição segura da API |
| Registro.br | domínio `marcelocarvalho.dev.br` | DNS do domínio próprio |

Endpoint público do MVP:

```text
https://api.marcelocarvalho.dev.br/api
```

O tráfego externo chega ao Nginx pelas portas 80/443. O Nginx encaminha as requisições para a API em `127.0.0.1:8080`. PostgreSQL permanece acessível somente pela rede interna do Docker Compose.

---

## 10. Segurança e rede

Tecnologias e mecanismos atualmente utilizados:

- autenticação SSH por chave pública;
- SSH na porta 51022;
- Fail2ban;
- Network Security Group e Security List da Oracle Cloud;
- iptables no host;
- UFW;
- Nginx na borda pública;
- HTTPS para acesso externo;
- porta 8080 restrita ao loopback;
- PostgreSQL sem exposição pública da porta 5432;
- credenciais da aplicação fornecidas por variáveis de ambiente e `.env`.

Autenticação JWT e autorização por roles não fazem parte do MVP e estão planejadas para a Fase 3.

---

## 11. Ferramentas de desenvolvimento

| Ferramenta | Versão registrada | Uso |
|---|---:|---|
| IntelliJ IDEA Community | versão exata não registrada | IDE de desenvolvimento |
| Git | versão exata não registrada | Controle de versão |
| GitHub | serviço | Repositório remoto |
| Maven Wrapper / Maven | conforme configuração do projeto | Build local |
| Docker | versão exata não registrada | Ambientes containerizados |

Ferramentas sem versão confirmada nos artefatos do projeto permanecem deliberadamente sem número. Isso evita transformar este documento histórico em uma fonte de versões presumidas.

---

## 12. Tecnologias planejadas, mas ainda não adotadas

As tecnologias abaixo pertencem às próximas fases e **não devem ser interpretadas como componentes atuais do MVP**.

### Fase 2

- Flyway para migrations e versionamento de schema.
- Categoria modelada como enum no domínio.
- Separação adicional entre usuário de migration e usuário de runtime do PostgreSQL.

### Fase 3

- JWT para autenticação.
- Autorização por roles.
- Monitoramento e métricas.
- Cache, se justificado pelas necessidades da aplicação.

### Fase 4

- React ou Vue.js para frontend, conforme decisão futura.

---

## 13. Resumo da stack do MVP v1.0.0

```text
Java 21
    |
Quarkus Platform 3.39.3
    |
Quarkus REST + Jackson
    |
Service + Repository/Panache
    |
Hibernate ORM
    |
PostgreSQL 16.4

Produção:
Internet
    |
HTTPS
    |
Nginx / Ubuntu 24.04 / Oracle Cloud ARM64
    |
127.0.0.1:8080
    |
Docker: Biblioteca API (Java 21)
    |
Docker network
    |
PostgreSQL 16.4 + volume persistente
```

---

## 14. Referências internas

- [Arquitetura](ARQUITETURA.md)
- [Regras de negócio](REGRAS_NEGOCIO.md)
- [Fases do projeto](FASES.md)
- [Como rodar](COMO_RODAR.md)
- [Deploy na VPS](DEPLOY_VPS_BIBLIOTECA_API.md)
- [Histórico](../HISTORY.md)

[Voltar ao README](../README.md)
