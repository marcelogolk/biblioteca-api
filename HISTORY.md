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

### 2026-09-18 - Implementação do CRUD de `Livro`: causa raiz, correções e regras de negócio (RN08-RN18)

#### Contexto

Ao revisar o CRUD de `Livro`, foi identificado um problema estrutural na raiz de
várias inconsistências percebidas nos testes, além de bugs pontuais e a
necessidade de mapear as validações de RN08 a RN18.

#### 🔴 Causa raiz: `Proprietario` como campo do DTO de entrada

O `LivroInclusaoDTO` original carregava um campo `Proprietario proprietario`
(a entidade JPA inteira), em vez de só o identificador. Isso causava o
seguinte fluxo problemático:

1. Cliente envia `{"proprietario": {"id": 5}}`
2. O Jackson instancia um objeto `Proprietario` só a partir do JSON — nunca
   gerenciado pela sessão do Hibernate
3. Ao persistir o `Livro` com essa referência, o Hibernate lança
   `TransientPropertyValueException` (instância "unsaved transient" no
   relacionamento `@ManyToOne`)
4. O `if (proprietario == null)` do código antigo só verificava ausência do
   campo, não a existência real do proprietário no banco — não cumprindo a
   RN11 de fato

**Correção:** os DTOs de entrada (`LivroInclusaoDTO`, `LivroAtualizacaoDTO`)
passaram a carregar apenas `Long proprietarioId`. O Service busca a entidade
real via `proprietarioRepository.findByIdOptional(id).orElseThrow(...)`,
retornando `404` se não existir, e só então monta/atualiza o `Livro` com uma
entidade de fato gerenciada pelo Hibernate.

#### Bugs concretos corrigidos

- **Checagem morta em `atualizarLivro`:** existia um `if (... == null)`
  comparando o retorno de um método que na verdade sempre lança exceção
  (nunca retorna `null`) — código inalcançável, escondendo o real caminho de
  erro. Substituído por `findByIdOptional(...).orElseThrow(...)`.
- **`atualizarLivro` sem checagem de livro inexistente:** o método buscava o
  `Livro` por `id` sem checar `null` antes de usá-lo, causando
  `NullPointerException` (500) em vez de `404` para um id inexistente.
  Corrigido buscando com `findByIdOptional(id).orElseThrow(...)` **antes**
  de buscar o proprietário, garantindo que o erro de "livro não encontrado"
  nunca seja mascarado por outro erro.
- **Controller retornando o DTO errado:** `atualizarLivro` no Controller
  devolvia `livroAtualizacaoDTO` (dado bruto enviado pelo cliente, sem
  `id`/`uuid`) em vez de `livroDTO` (resultado real, vindo do banco).
  Corrigido para devolver o DTO de saída correto.
- **Cosmético:** vírgula/espaço faltando no `toString()` de `Livro` e
  `Proprietario`.

#### Decisão: `equals`/`hashCode` via business key, não via `id`

Seguindo a recomendação do Hibernate para entidades JPA, `equals()` e
`hashCode()` de `Livro` e `Proprietario` passaram a se basear em uma
**business key** (`uuid` em `Livro`, `cpf` em `Proprietario`) em vez do
`id` gerado pelo banco. Motivos:

- O `id` é `null` até o `persist()`; se a entidade entrar em um `HashSet`
  antes de ser salva, seu hash muda depois — quebrando o contrato do `Set`
- A business key existe desde a criação do objeto e nunca muda, mantendo o
  hash estável durante todo o ciclo de vida da entidade
- A comparação usa `o instanceof Livro livro` em vez de
  `getClass() != o.getClass()`, para funcionar corretamente também quando
  `o` é um proxy do Hibernate (lazy loading), já que o proxy é uma subclasse
  em tempo de execução — `instanceof` reconhece essa herança nativamente

#### Decisão: tipo `Year` para `anoDePublicacao` + `AttributeConverter`

O campo `anoDePublicacao` foi definido como `java.time.Year` (em vez de
`int`), para permitir a validação `@PastOrPresent` nativamente (Bean
Validation não suporta essa anotação sobre tipos primitivos). Como nem JPA
nem Hibernate mapeiam `Year` para coluna automaticamente, foi criada
`YearAttributeConverter` (`@Converter(autoApply = true)`, pacote
`entity.converter`), convertendo `Year ↔ Integer` na coluna do banco.

#### Decisão: DTOs separados por operação (padrão já usado em `Proprietario`)

- `LivroDTO`: saída (dados já persistidos, sem validação — dado que sai do
  banco já é confiável)
- `LivroInclusaoDTO`: entrada na criação, com todas as validações de
  RN08, RN09/RN11 (parcial), RN10, RN12, RN13, RN14
- `LivroAtualizacaoDTO`: entrada na atualização, com as mesmas validações
  de `LivroInclusaoDTO` (RN16), **exceto `proprietarioId`**

#### Regras de negócio mapeadas (RN08-RN18)

| Regra | Onde é resolvida |
|---|---|
| RN08 (título obrigatório) | `@NotBlank` em `titulo` (Entity + DTOs de entrada) |
| RN09 (proprietário obrigatório) | `@NotNull` em `proprietarioId` (DTOs de entrada) |
| RN10 (título mín. 3 caracteres) | `@Size(min = 3)` em `titulo` |
| RN11 (proprietário deve existir) | Service: `findByIdOptional(...).orElseThrow(404)` — não é validável via anotação, pois depende do estado do banco |
| RN12 (categoria obrigatória) | `@NotBlank` em `categoria` |
| RN13 (ano não pode ser futuro) | `@PastOrPresent` em `anoDePublicacao` (tipo `Year`) |
| RN14 (autor obrigatório) | `@NotBlank` em `autor` |
| RN15 (proprietário imutável na atualização) | `proprietarioId` **removido** de `LivroAtualizacaoDTO`; Service nunca busca nem seta proprietário em `atualizarLivro` |
| RN16 (mesmas validações no update) | `LivroAtualizacaoDTO` espelha as anotações de `LivroInclusaoDTO` |
| RN17 (exclusão sem restrição) | `deletarLivroById` sem condição além de existência do registro |
| RN18 (exclusão não afeta proprietário) | Garantido pela direção do relacionamento (`Livro` é o lado dono da FK); atenção para nunca configurar `cascade = REMOVE`/`orphanRemoval` no sentido `Proprietario → Livro` |

#### Aprendizados

- DTO de entrada nunca deve carregar uma entidade JPA inteira como campo —
  só o identificador, com a entidade real buscada no Service
- Regras que dependem do estado atual do banco (unicidade, existência de
  relacionamento) não são resolvidas por Bean Validation isolado; exigem
  checagem explícita no Service
- `equals`/`hashCode` de entidade JPA merece atenção redobrada: usar
  business key, e comparar com `instanceof` (ou `Hibernate.getClass`) para
  lidar corretamente com proxies de lazy loading
- Ao restringir uma operação (RN15), a forma mais simples e segura é não
  expor o campo no DTO, em vez de expô-lo e validar sua imutabilidade depois

---
### 2026-09-30 - Correção: Comparação de identificadores e revisão de configuração

#### Contexto

Durante revisão dos endpoints de busca de `Livro`, foi identificado um trecho
comparando identificadores do tipo `Long` usando o operador `==` em vez de
`.equals()`. Esse tipo de comparação é instável para objetos wrapper, podendo
falhar silenciosamente dependendo do valor (fora da faixa de cache de
`Long` em Java, `==` compara referência de objeto, não valor).

Também foi identificado um possível erro de digitação em
`application.properties`, na chave
`%prod.quarkus.hernate-orm.database.generation=update` (ausência da letra "i"
em "hibernate").

#### Decisão

A comparação de `Long` via `==` foi corrigida para `.equals()`. O typo em
`application.properties` foi registrado como pendência de correção.

#### Justificativa

- `.equals()` garante comparação por valor, independente de cache de
  `Integer`/`Long` do Java, evitando bugs sutis e intermitentes.
- Uma chave de configuração mal escrita falha silenciosamente: o Quarkus
  simplesmente ignora a propriedade não reconhecida, em vez de lançar erro,
  então o problema só seria percebido por comportamento inesperado em runtime.

#### Ação

- Corrigida a comparação de `id` em `Livro` para usar `.equals()`.
- **Pendência registrada:** revisar e corrigir o nome da propriedade em
  `application.properties`.

---

### 2026-09-30 - Decisão: Busca de `Livro` por categoria e por proprietário filtrada no banco

#### Contexto

Os endpoints de busca de `Livro` por categoria e por proprietário estavam
implementados buscando todos os registros via `listAll()` e filtrando o
resultado em memória (via stream). Essa abordagem não escala bem à medida que
a tabela cresce, pois traz todos os registros do banco para a aplicação antes
de descartar os que não interessam.

#### Decisão

Ambas as buscas passaram a delegar o filtro diretamente ao banco de dados,
via Panache, em vez de filtrar em memória.

#### Justificativa

- Filtrar no banco evita trafegar e carregar na memória da aplicação
  registros que serão descartados logo em seguida — importante à medida que
  o volume de dados cresce.
- O problema de suportar muitas requisições simultâneas é um tema distinto
  (concorrência/escalabilidade de infraestrutura), não resolvido por esta
  mudança, e fica como estudo futuro.

#### Ação

- Criado o método `findByProprietarioId` em `LivroRepository`, usando a API
  do Panache (`list("proprietario.id", proprietarioId)`).
- `LivroService.buscarLivroByProprietarioId` passou a chamar o Repository e
  converter o resultado via `.stream().map(LivroDTO::new).toList()`.
- Busca por categoria migrada de forma equivalente, filtrando via consulta
  no Repository em vez de `listAll()` com filtro em memória.
- Removido o método morto `isCategoriaExiste`, remanescente de uma
  abordagem de busca por categoria descartada.
- Confirmado, via log de SQL gerado pelo Hibernate (cláusula
  `where l1_0.proprietario_id=?`), que o filtro de fato ocorre no banco.
- Decisão de **não impedir** o cadastro de livros repetidos, por enquanto.
- **Pendência registrada:** revisitar o uso de path param
  (`/categoria/{categoria}`) versus query param na busca por categoria — um
  segmento de rota vazio resulta em `404` em vez de alcançar o método, o que
  pode não ser o comportamento desejado.

---

### 2026-09-30 - Decisão: Consolidação dos testes de integração de `LivroController`

#### Contexto

Existiam duas classes de teste de integração para `LivroController`:
`LivroControllerIntegrationTest` (cobrindo atualização, com ciclo
`@BeforeEach`/`@AfterEach`) e `LivroControllerIncluirLivroIntegrationTest`
(cobrindo inclusão, com ciclo `@BeforeAll`/`@AfterAll` e nove cenários via
`@ParameterizedTest`, compartilhando um único proprietário entre todos os
cenários). A divergência de ciclo de vida entre as duas gerava inconsistência
de abordagem e dificultava a manutenção.

Era desejável consolidar as duas em uma única classe, usando um único padrão
de isolamento. Avaliou-se dois modelos: reaproveitar estado entre cenários
(mais rápido, já em uso na classe de inclusão) versus recriar o estado
(proprietário) a cada teste individual (mais lento, porém com isolamento
mais simples e previsível).

#### Decisão

Consolidação das duas classes em uma única, `LivroControllerTest`, adotando
o modelo de **estado fresco por teste** — um novo proprietário criado via
`@BeforeEach` e removido via `@AfterEach` a cada execução, priorizando
isolamento e simplicidade sobre velocidade de execução da suíte.

#### Justificativa

- Isolamento por teste elimina dependência de ordem de execução e de estado
  deixado por testes anteriores, reduzindo risco de testes "frágeis"
  (flaky tests).
- A simplicidade de um único padrão de ciclo de vida facilita a leitura e
  manutenção futura da suíte.

#### Decisão técnica: `@MethodSource` dos cenários parametrizados

Ao tentar dar aos nove cenários de inclusão acesso ao `proprietarioId`
criado no `@BeforeEach` de cada execução, investigou-se se bastaria tornar o
método de `@MethodSource` não-estático (via
`@TestInstance(Lifecycle.PER_CLASS)`). Constatou-se que esse método é sempre
resolvido **uma única vez**, antes de qualquer `@BeforeEach`, independente do
ciclo de vida da classe (`PER_METHOD` ou `PER_CLASS`) — portanto, ele nunca
teria acesso a um `proprietarioId` gerado "por teste".

**Solução adotada:** o `@MethodSource` permanece `static`; cada cenário usa
um texto-marcador (`__PROPRIETARIO_ID__`) no lugar do id no corpo JSON. Esse
marcador é substituído pelo `proprietarioId` real, via `String.replace(...)`,
dentro do próprio método de teste — que já roda depois do `@BeforeEach` da
execução correspondente.

#### Ação

- Criada `LivroControllerTest`, substituindo `LivroControllerIntegrationTest`
  e `LivroControllerIncluirLivroIntegrationTest`.
- `@BeforeEach` cria um proprietário por execução, com CPF gerado via
  `System.nanoTime()` (em vez de `System.currentTimeMillis()`), reduzindo o
  risco de colisão de CPF decorrente do `@BeforeEach` agora rodar várias
  vezes seguidas na mesma suíte (uma vez por teste, incluindo cada um dos
  nove cenários parametrizados).
- `@AfterEach` remove o proprietário e todos os livros criados durante o
  teste, rastreados numa lista de instância (`livrosCriados`).
- Adicionados testes para o endpoint `DELETE /api/livros/{id}`, cobrindo os
  dois caminhos possíveis: exclusão bem-sucedida (`204`, com confirmação
  adicional via `GET` subsequente retornando `404`) e id inexistente
  (`404`).
- Analisado o endpoint `GET /api/livros` (`listAllLivros`): por não receber
  nenhum parâmetro, não existem caminhos de erro possíveis — apenas
  variações de estado dos dados (zero, um ou vários livros). Implementação
  do teste de "lista vazia" ficou pendente (ver próxima entrada).

#### Aprendizados

- `@MethodSource` é resolvido uma única vez, antes de qualquer
  `@BeforeEach`/`@Test` — independente do ciclo de vida da classe. Dados que
  dependem de setup por teste não podem ser obtidos diretamente dentro dele;
  é necessário um mecanismo de substituição posterior (placeholder) dentro
  do próprio método de teste.

---

### 2026-09-30 - Decisão: Adiamento do isolamento do banco de dados usado nos testes

#### Contexto

Ao planejar o teste do cenário "lista vazia" para `GET /api/livros`,
constatou-se que o banco de dados usado pela suíte de testes automatizados é
o mesmo banco usado manualmente durante o desenvolvimento (via Swagger e
DBeaver). Isso inviabiliza testar com segurança uma afirmação de estado
absoluto da tabela (como "a lista está vazia"), pois o resultado depende de
dados inseridos manualmente fora do controle do teste — e uma tentativa de
limpar a tabela antes do teste arriscaria apagar dados de desenvolvimento
sem intenção.

#### Decisão

Por ora, mantido o banco de desenvolvimento compartilhado também para os
testes automatizados. A migração para um banco de testes isolado (via
Quarkus Dev Services/Testcontainers, ou configuração de datasource
dedicada) fica adiada para um momento futuro.

#### Justificativa

- A maioria dos testes já escritos não é afetada por esse compartilhamento,
  pois seguem o padrão de criar seus próprios dados (via `@BeforeEach`) e
  verificar apenas recursos específicos criados por eles mesmos — nunca o
  estado absoluto da tabela inteira.
- Apenas o cenário de "lista vazia" exige estado absoluto do banco, sendo o
  único caso realmente bloqueado por este compartilhamento no momento.
- Migrar o datasource de teste é uma mudança de configuração do projeto, não
  do código das classes de teste — podendo ser feita depois sem exigir
  reescrever os testes já existentes.

#### Ação

- **Pendência registrada:** investigar isolamento do banco de testes via
  Quarkus Dev Services/Testcontainers.
- **Pendência registrada:** implementar teste de "lista vazia" para
  `GET /api/livros`, condicionado à resolução do isolamento de banco, ou,
  como alternativa imediata, usando verificação relativa de contagem
  (quantidade antes/depois, em vez de valor absoluto).

---
### 2026-10-03 - Implementação de ProprietarioControllerTest

### Contexto

ProprietarioControllerTest existia apenas como esqueleto (imports, setUp/
tearDown vazios e métodos vazios para os cinco endpoints), sem @QuarkusTest
nem uso de RestAssured. Era necessário construir a suíte de testes de
integração do ProprietarioController, seguindo o mesmo padrão já consolidado
em LivroControllerTest.Decisão

Adotada a mesma estrutura de isolamento por teste já usada em
LivroControllerTest: @AfterEach removendo os proprietários criados durante
cada execução, rastreados em uma lista de instância (proprietariosCriados), e
um helper criarProprietario(nome) responsável por gerar um CPF único via
System.nanoTime() a cada chamada.O helper foi projetado para retornar não apenas o id do proprietário criado,
mas também o cpf gerado, através de um record local
(ProprietarioCriado(Long id, String cpf)), permitindo que os testes validem
o campo cpf sem precisar recalculá-lo ou supor seu valor.Justificativa


Reaproveitar o mesmo padrão de isolamento de LivroControllerTest mantém
consistência entre as suítes e reduz a curva de aprendizado para manutenção
futura.
Expor o cpf gerado via record evita duplicar a lógica de geração de CPF
dentro de cada teste e permite asserções completas (id, nome e cpf) nos
cenários que criam um proprietário via helper.

### Ação

Implementados os testes para os cinco endpoints do ProprietarioController:
listAllProprietarios: teste relativo de contagem (quantidade antes/depois
da criação de novos proprietários), no mesmo espírito do teste pendente para
GET /api/livros, evitando depender do estado absoluto da tabela.
buscarProprietarioById: cenários de id existente (validando id, nome e
cpf do retorno) e id inexistente (404).
incluirProprietario: sete cenários via @ParameterizedTest/@MethodSource
(caminho feliz, nome ausente, nome com 2 caracteres, nome com exatamente 3
caracteres, CPF ausente, CPF com menos de 11 dígitos, CPF com máscara),
usando um placeholder (__CPF__) substituído por um CPF único gerado no
próprio método de teste — mesma técnica de placeholder já usada em
LivroControllerTest para proprietarioId. Adicionado também um teste
isolado para CPF duplicado (409), por depender de um proprietário
previamente existente e não se encaixar no @MethodSource dos demais
cenários.
atualizarProprietario: caminho feliz, id inexistente (404) e três
cenários de validação do campo nome via @MethodSource (ausente, 2
caracteres, exatamente 3 caracteres).
excluirProprietario: exclusão bem-sucedida (204, com confirmação
adicional via GET subsequente retornando 404) e id inexistente (404).
Pendência registrada: assim como em LivroControllerTest, os testes
relativos de contagem (listAllProprietarios e listAllLivros) ficam
condicionados à futura resolução do isolamento do banco de testes — caso esse
isolamento seja implementado, pode valer a pena revisitar esses testes para
afirmações de estado absoluto, mais simples de ler que a verificação relativa.
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
