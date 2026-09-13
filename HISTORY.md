# 📖 Histórico de Desenvolvimento

Registro do desenvolvimento, decisões arquiteturais, problemas encontrados e soluções implementadas.

---

## [v1.0.0] - Em Desenvolvimento

### 2026-09-12 - Inicialização do Projeto

#### Decisões Tomadas

1. **Framework:** Escolhido Quarkus por ser supersônico e otimizado para Java
2. **Build Tool:** Maven para gerenciar dependências e build
3. **Banco de Dados:** PostgreSQL para persistência relacional
4. **ORM:** Hibernate ORM com Panache para simplificar operações
5. **Documentação:** SmallRye OpenAPI com Swagger UI
6. **Testes:** JUnit 5 com cobertura mínima de 70%
7. **Arquitetura:** Camadas (Controller → Service → Repository)
8. **Versionamento:** Git + GitHub para controle de versão
9. **Ambiente:** VPS Oracle Cloud Free Tier para produção

#### Estrutura Inicial

- Criado repositório `biblioteca-api` no GitHub
- Gerado projeto Quarkus com extensões:
    - RESTEasy Classic Jackson
    - Hibernate ORM with Panache
    - PostgreSQL JDBC Driver
    - SmallRye OpenAPI
    - Agroal (Connection Pool)
- Estrutura de pastas definida (controller, service, repository, entity, dto)
- Documentação inicial criada (README, FASES, REGRAS_NEGOCIO, etc)

#### Aprendizados

- Quarkus oferece excelente suporte para desenvolvimento rápido
- Panache simplifica muito o código de persistência
- A estrutura em camadas é fundamental para manutenibilidade

---

## [v1.1.0] - Planejado

- Melhorias em funcionalidades e tratamento de erros.
    - transformar o campo categoria do livro em um enum

---

## [v1.2.0] - Planejado

Segurança e otimizações de performance.

---

## [v2.0.0] - Planejado

Frontend para consumir a API.

---

**Última atualização:** 2026-09-12