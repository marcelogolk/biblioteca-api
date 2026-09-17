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

### 2026-09-17 - Decisão: Escopo da Validação de CPF e Adiamento da Validação de Dígitos Verificadores

#### Contexto

Com a introdução do `cpf` como Chave de Negócio no cadastro de `Proprietario`, fez-se necessário definir a estratégia de validação do parâmetro de entrada no `ProprietarioInclusaoDTO`. Avaliou-se dois níveis de validação:

1. **Validação Formato/Sintática:** Checar apenas a obrigatoriedade (`@NotBlank`) e se a String contém exatamente 11 dígitos numéricos via Expressão Regular (`@Pattern`).
2. **Validação Algorítmica/Semântica:** Calcular e validar matematicamente os dígitos verificadores (usando anotações como `@CPF` do Hibernate Validator ou consumindo uma API/biblioteca externa de validação).

#### Decisão

Optado por manter a **Validação Sintática por formato (`@Pattern(regexp = "\\d{11}")`) na Fase 1 (MVP)**, postergando a validação algorítmica/matemática do dígito verificador para a **Fase 2**.

#### Justificativa

- **Foco no MVP (Fase 1):** A validação por regex atende integralmente ao formato aceito pelo modelo relacional e previne erros de parse ou payloads malformados na API.
- **Evolução Incremental para a Fase 2:** Pretende-se avaliar na Fase 2 se a validação algorítmica será feita via dependência do Bean Validation (`@CPF`) ou via integração com uma API de consulta/validação externa. Mover essa decisão para a próxima iteração evita complexidade desnecessária no MVP.

#### Ação

- Atualizado o arquivo `REGRAS_NEGOCIO.md` com renumeração das regras inserindo as regras `RN05`, `RN06` e `RN07`.
- Registrada a tarefa de integração de validação de dígito verificador/API externa de CPF no roadmap da **Fase 2** no arquivo `FASES.md`.

### 2026-09-16 - Decisão: CPF como Identidade do Proprietário (Imutável) e Revisão da Unicidade de Nome

#### Contexto

O campo `cpf` foi introduzido na entidade `Proprietario` originalmente para resolver
um problema de modelagem em `equals()`/`hashCode()`: usar o `id` técnico (gerado
pelo banco) como base de igualdade é frágil, especialmente antes da entidade ser
persistida. CPF, por ser um identificador real de pessoa no mundo físico, é um
candidato mais correto a "chave de negócio".

Ao expor `cpf` também como campo editável no `PUT /proprietarios/{id}` (reusando o
mesmo DTO da criação), surgiu uma contradição: se CPF representa a identidade do
proprietário, permitir sua alteração via atualização normal do cadastro não faz
sentido — mudar o CPF seria, na prática, dizer que se trata de outra pessoa.

Isso gerou uma série de tentativas de resolver o problema via validação em tempo de
execução (bloquear com HTTP 409 quando o CPF enviado divergia do salvo, checando
duplicidade contra o próprio registro em edição), incluindo uma tentativa
descartada de "deletar e recriar" o proprietário internamente para simular uma
"troca" de CPF sem violar a constraint `updatable = false` do banco — abordagem
rejeitada por quebrar a identidade do recurso (o `id` mudava a cada atualização de
CPF) e por gerar risco de violação de integridade referencial com `Livro` assim que
essa entidade tiver registros associados.

#### Decisão

1. **CPF é imutável após o cadastro.** Não existe nenhum fluxo da API que permita
   alterar o CPF de um proprietário já existente. Se o CPF foi cadastrado com erro,
   a correção deve ser feita excluindo o proprietário (`DELETE`) e cadastrando um
   novo (`POST`) — nunca através do endpoint de atualização.
2. Para tornar essa regra estrutural (e não apenas validada), foi criado um DTO de
   atualização específico, `ProprietarioAtualizacaoDTO`, contendo somente o campo
   `nome`. O `PUT /proprietarios/{id}` passou a aceitar exclusivamente esse DTO —
   não há campo `cpf` no contrato de entrada da atualização, então não existe a
   possibilidade de o cliente sequer tentar alterá-lo.
3. **RN01 ("nome deve ser único") foi revogada.** Antes da existência do CPF, o
   nome era o único dado disponível para tentar diferenciar proprietários, o que
   levou à decisão original de torná-lo único. Essa premissa deixou de fazer
   sentido: no mundo real, homônimos existem (inclusive entre parentes, como pai e
   filho com o mesmo nome), e o nome pode legitimamente mudar ao longo da vida de
   uma pessoa — ao contrário do CPF. Com CPF assumindo o papel de identidade única
   e imutável, a restrição de unicidade sobre `nome` tornou-se uma regra artificial
   e desalinhada com o domínio.

#### Justificativa

- Impedir a alteração de CPF **estruturalmente** (via forma do DTO) é mais robusto
  do que impedir via validação em tempo de execução: elimina uma classe inteira de
  bugs de lógica condicional (comparação de valores, checagem de duplicidade
  "excluindo a si mesmo"), que se mostrou propensa a erro nas primeiras tentativas.
- Separar DTOs de criação e atualização (`ProprietarioInclusaoDTO` vs.
  `ProprietarioAtualizacaoDTO`) deixa explícito, só pela assinatura do contrato,
  quais campos cada operação realmente aceita — sem depender de o desenvolvedor
  lembrar de validar isso manualmente a cada alteração futura.
- Remover a unicidade de `nome` alinha o modelo de dados à realidade do domínio
  (pessoas podem ser homônimas) sem abrir mão de identidade única de fato, já que
  essa responsabilidade passou integralmente para o CPF.

#### Ação

- `Proprietario.java`: removido `unique = true` da coluna `nome` (mantido
  `nullable = false`); `cpf` permanece `unique = true, nullable = false,
  updatable = false`, sem setter público.
- Criado `ProprietarioAtualizacaoDTO(String nome)`, usado exclusivamente pelo
  `PUT /proprietarios/{id}`.
- `ProprietarioService.atualizarProprietario` simplificado para apenas buscar por
  id, retornar 404 se não encontrado, e atualizar o nome — sem qualquer lógica de
  comparação ou validação de CPF.
- `ProprietarioService.incluirProprietario` mantém a checagem de duplicidade de
  CPF (`isCPFJaExiste`), retornando 409 em caso de conflito; a checagem
  equivalente para nome foi removida.
- **Pendência registrada:** o cenário de excluir um proprietário para corrigir um
  CPF errado, quando esse proprietário já possui livros associados, ainda depende
  da resolução da RN04 (bloqueio de exclusão de proprietário com livros) — em
  aberto até a implementação do CRUD de `Livro`.
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
