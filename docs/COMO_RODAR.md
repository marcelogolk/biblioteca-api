# 🚀 Como Rodar Localmente

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21+** — [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **Docker e Docker Compose** — [Download](https://docs.docker.com/get-docker/)
- **Git** — [Download](https://git-scm.com/)
- **IntelliJ IDEA Community** — [Download](https://www.jetbrains.com/idea/download/)

## Verificar Instalação

```bash
java -version
mvn -version
docker --version
docker compose version
git --version
```

---

## 1. Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/biblioteca-api.git
cd biblioteca-api
```

---

## 2. Configurar Banco de Dados

O banco de dados roda em container Docker, seguindo a mesma estrutura usada no
deploy (veja [Deploy da Biblioteca API](DEPLOY_VPS_BIBLIOTECA_API.md)).

### Criar o arquivo de variáveis de ambiente

Dentro da pasta `local/`, copie o arquivo de exemplo e preencha com valores
próprios:

```bash
cd local
cp .env.example .env
```

Edite o `.env` preenchendo as credenciais (nunca reutilize as mesmas senhas de
produção):

``` .env

POSTGRES_DB=biblioteca_api POSTGRES_ADMIN_USER=troque_aqui
POSTGRES_ADMIN_PASSWORD=troque_aqui APP_DB_USER=troque_aqui APP_DB_PASSWORD=troque_aqui

```

### Subir o container do Postgres

``` Bash
docker compose up -d
docker compose logs -f postgres

```
Confirme nos logs que o script de inicialização rodou sem erros (criação da
role de aplicação e `GRANT` de privilégios).

### Configurar credenciais no projeto

O `application.properties` já está preparado com perfis (`%dev`/`%prod`) e lê
usuário e senha a partir de variáveis de ambiente — não é necessário editar o
arquivo manualmente:

---

```properties
# ---------- Comum a todos os ambientes ----------
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${APP_DB_USER}
quarkus.datasource.password=${APP_DB_PASSWORD}
quarkus.hibernate-orm.log.sql=true

# Prefixo global da API REST
quarkus.rest.path=/api

# ---------- Perfil prod (VPS) ----------
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/biblioteca_api
%prod.quarkus.hibernate-orm.database.generation=update


%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca_api
%dev.quarkus.hibernate-orm.database.generation=update

# ---------- Perfil test (ambiente de testes) ----------
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca_api
%test.quarkus.hibernate-orm.database.generation=update
```

---

## 3. Rodar em Modo Desenvolvimento

Como o Quarkus não carrega o `.env` automaticamente, exporte as variáveis no
shell antes de iniciar a aplicação:

```bash
# Ainda dentro da pasta local/
set -a
source .env
set +a

# Voltar para a raiz do projeto
cd ..
./mvnw quarkus:dev
```

Você verá uma saída similar a:
```
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
---
```

## 4. Acessar a Aplicação

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/q/swagger-ui/
- **Dev UI:** http://localhost:8080/q/dev/

---

## 5. Rodar Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com cobertura
./mvnw test jacoco:report
```

**Nota:** os testes de integração atualmente usam o mesmo banco configurado
acima (compartilhado com o uso manual em desenvolvimento). Evite depender, em
novos testes, de verificações sobre o estado absoluto do banco (como "a
tabela está vazia") — veja detalhes no `HISTORY.md`.

---

## 6. Build para Produção

```Bash

# Gerar JAR
./mvnw package

# Executar JAR
java -jar target/quarkus-app/quarkus-run.jar
```

Para o fluxo completo de deploy em VPS (Docker, Postgres em container, API
containerizada, reverse proxy), veja
[Deploy da Biblioteca API — Oracle Cloud VPS](DEPLOY_VPS_BIBLIOTECA_API.md).

---

## Troubleshooting

### Erro: "Connection refused" ao conectar no PostgreSQL

- Verifique se o PostgreSQL está rodando
- Confirme as credenciais em `application.properties`
- Verifique se o banco `biblioteca_db` foi criado

### Erro: "Port 8080 already in use"**

```bash
# Mudar porta em application.properties
quarkus.http.port=8081
```
### Erro: "Hibernate dialect not found"

- Certifique-se de que o driver PostgreSQL está no `pom.xml`
- Verifique se `quarkus.datasource.db-kind=postgresql` está configurado

### Erro ao rodar os scripts de inicialização do banco

- Confirme que a pasta `init/` foi criada manualmente antes de subir o
  container (evita problemas de permissão)
- Confirme que o script tem permissão de execução:
  `chmod +x local/init/01-create-app-user.sh`
- Se o script foi alterado após a primeira execução, é necessário recriar o
  volume: `docker compose down -v` e subir novamente

---

## Referências

- [Documentação Quarkus](https://quarkus.io/guides/)
- [Documentação Docker Compose](https://docs.docker.com/compose/)
- [Hibernate ORM](https://hibernate.org/orm/)

---

[Voltar ao README](../README.md)