# 🚀 Como Rodar Localmente

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 25+** — [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 12+** — [Download](https://www.postgresql.org/download/)
- **Git** — [Download](https://git-scm.com/)
- **IntelliJ IDEA Community** — [Download](https://www.jetbrains.com/idea/download/)

## Verificar Instalação

```bash
java -version
mvn -version
psql --version
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

### Criar banco de dados PostgreSQL

```bash
# Conectar ao PostgreSQL
psql -U postgres

# Criar banco de dados
CREATE DATABASE biblioteca_db;

# Criar usuário (opcional, mas recomendado)
CREATE USER biblioteca_user WITH PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE biblioteca_db TO biblioteca_user;

# Sair
\q
```

### Configurar credenciais no projeto

Edite o arquivo `src/main/resources/application.properties`:

```properties
# Banco de Dados
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=sua_senha
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca_db

# Hibernate
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.log.sql=true
```

---

## 3. Rodar em Modo Desenvolvimento

```bash
# No diretório raiz do projeto
./mvnw quarkus:dev
```

Você verá uma saída similar a:
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
---

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

## 6. Build para Produção
```Copiar

# Gerar JAR
./mvnw package

# Executar JAR
java -jar target/quarkus-app/quarkus-run.jar
```
---

## Troubleshooting

### Erro: "Connection refused" ao conectar no PostgreSQL

- Verifique se o PostgreSQL está rodando
- Confirme as credenciais em `application.properties`
- Verifique se o banco `biblioteca_db` foi criado

### Erro: "Port 8080 already in use"

```bash
# Mudar porta em application.properties
quarkus.http.port=8081
```
### Erro: "Hibernate dialect not found"

- Certifique-se de que o driver PostgreSQL está no `pom.xml`
- Verifique se `quarkus.datasource.db-kind=postgresql` está configurado

---
## Referências

- [Documentação Quarkus](https://quarkus.io/guides/)
- [Guia PostgreSQL](https://www.postgresql.org/docs/)
- [Hibernate ORM](https://hibernate.org/orm/)

---

[Voltar ao README](../README.md)
