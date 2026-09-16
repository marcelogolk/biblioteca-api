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

### 2026-09-14 - Decisão: Validação de Regras de Negócio via Bean Validation

#### Contexto

As regras de negócio do projeto (RN02, RN03, RN07, entre outras) exigem validações
de campos como obrigatoriedade, tamanho e formato. Havia duas abordagens possíveis:

1. Validar manualmente dentro da camada Service (ifs explícitos lançando exceções)
2. Usar Bean Validation (JSR 380) via anotações nas entidades/DTOs (`@NotBlank`,
   `@Size`, `@Min`, etc.), delegando a validação ao Hibernate Validator

#### Decisão

Optado pela abordagem com **Bean Validation**, usando a extensão
`quarkus-hibernate-validator`.

#### Justificativa

- Já existia a extensão `quarkus-hibernate-orm-panache` no projeto; a
  `quarkus-hibernate-validator` complementa naturalmente esse ecossistema
- Validações declarativas via anotação deixam a regra de negócio visível
  diretamente no modelo (entidade/DTO), reduzindo duplicação de código de
  validação espalhado pelos Services
- Integração nativa com o Quarkus REST: usando `@Valid` nos parâmetros dos
  endpoints, a validação ocorre automaticamente antes de chegar à camada de
  negócio, retornando `400 Bad Request` com o detalhamento dos erros
- Reserva-se a validação manual no Service apenas para regras que dependem de
  estado externo (ex.: verificar duplicidade no banco), que Bean Validation
  não cobre isoladamente

#### Ação

Adicionada a dependência ao `pom.xml`, sem versão fixa, seguindo o padrão de
deixar o `quarkus-bom` gerenciar a versão:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```

---
### 2026-09-16 - Decisão: Implementação do `equals()` e `hashCode()` usando Chave de Negócio (Business Key)

#### Contexto

Na modelagem das entidades JPA (como `Usuario` e `Leitor`), era necessário definir a estratégia para comparação de igualdade e geração de hash (`equals()` e `hashCode()`). Havia três abordagens possíveis:

1. Utilizar a Primary Key (`@Id` gerada pelo banco via `Long`)
2. Comparar instâncias via referência de memória ou operadores relacionais (`==`)
3. Utilizar uma Chave de Negócio (Business Key), como o CPF ou uma chave única natural imutável

#### Decisão

Optado pela implementação do `equals()` e `hashCode()` baseada em **Chave de Negócio (Business Key)** imutável (ex.: `cpf`).

#### Justificativa

- **Consistência de Estado no Ciclo de Vida do JPA:** Identificadores gerados pelo banco de dados (`@GeneratedValue`) iniciam como `null` no estado transiente (antes de persistir). Se a comparação depender do `id`, entidades não persistidas serão consideradas iguais entre si ou sofrerão alteração no valor retornado por `hashCode()` ao transitar para o estado gerenciado/persistido.
- **Integridade em Coleções (`Set` / `Map`):** A alteração do valor de `hashCode()` após a atribuição do `id` quebra o contrato de coleções Java baseadas em hash (como `HashSet`), podendo fazer com que o objeto fique "inacessível" na coleção.
- **Recomendação da Documentação do Hibernate:** O Hibernate recomenda expressamente o uso de chaves de negócio naturais únicos e imutáveis para `equals()` e `hashCode()`, garantindo que a igualdade em memória seja estável desde o momento em que a entidade é instanciada (`new Entidade()`).
- **Correção na Comparação:** Evita-se o erro comum de comparar instâncias ou invocar o operador relacional `==` com objetos do tipo wrapper (`Long`), que pode falhar em casos onde o *autoboxing* ou *caching* de instâncias do Java não se aplica.

#### Ação

Implementado o método `equals()` comparando o campo de negócio imutável (`cpf`) via `Objects.equals()`, e o `hashCode()` derivado desse mesmo atributo[cite: 1]. Garantiu-se a restrição de imutabilidade desse campo no modelo JPA (`@Column(updatable = false, nullable = false, unique = true)`) e a remoção de *setters* para a chave de negócio, promovendo a atribuição obrigatória do valor via construtor da entidade[cite: 1].

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

**Última atualização:** 2026-09-14
