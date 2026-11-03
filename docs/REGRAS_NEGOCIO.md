# Regras de Negócio

## Objetivo

Este documento consolida as regras funcionais da Biblioteca API e identifica o estado de cada regra no planejamento do projeto.

As regras marcadas como **vigentes no MVP** fazem parte da versão 1.0.0. As regras marcadas como **planejadas para a Fase 2** permanecem válidas como objetivo de negócio, mas ainda dependem de implementação ou refinamento técnico.

---

## 1. Proprietário

### 1.1 Cadastro

#### RN01 - Nome obrigatório

O nome do proprietário é obrigatório.

**Estado:** vigente no MVP.

#### RN02 - Tamanho do nome

O nome do proprietário deve conter entre 3 e 100 caracteres.

**Estado:** vigente no MVP.

#### RN03 - CPF obrigatório

O CPF é obrigatório no cadastro do proprietário.

**Estado:** vigente no MVP.

#### RN04 - Formato do CPF

O CPF deve ser informado como uma sequência de exatamente 11 dígitos numéricos.

Na versão 1.0.0, a API valida apenas o formato do CPF. A validação algorítmica dos dígitos verificadores está planejada para a Fase 2.

**Estado:** validação sintática vigente no MVP; validação algorítmica planejada para a Fase 2.

#### RN05 - Unicidade do CPF

Não pode haver dois proprietários cadastrados com o mesmo CPF.

O CPF funciona como a chave de negócio do proprietário. O nome não é único, pois proprietários diferentes podem possuir o mesmo nome.

**Estado:** vigente no MVP.

### 1.2 Atualização

#### RN06 - CPF imutável

O CPF não pode ser alterado depois do cadastro.

A restrição é estrutural: o contrato de atualização do proprietário contém somente o nome e não expõe o CPF. Caso um CPF tenha sido cadastrado incorretamente, a correção exige a exclusão do cadastro e a criação de um novo proprietário, respeitando as regras de exclusão vigentes.

**Estado:** vigente no MVP.

#### RN07 - Validação do nome na atualização

A atualização do proprietário deve respeitar as mesmas regras de obrigatoriedade e tamanho aplicadas ao nome no cadastro.

**Estado:** vigente no MVP.

### 1.3 Exclusão

#### RN08 - Bloqueio de exclusão com livros associados

Um proprietário que possua livros associados não deve ser excluído.

**Estado:** vigente no MVP.

---

## 2. Livro

### 2.1 Cadastro

#### RN09 - Título obrigatório

O título do livro é obrigatório.

**Estado:** vigente no MVP.

#### RN10 - Tamanho mínimo do título

O título deve possuir pelo menos 3 caracteres.

**Estado:** vigente no MVP.

#### RN11 - Autor obrigatório

O autor é obrigatório.

**Estado:** vigente no MVP.

#### RN12 - Categoria obrigatória

A categoria é obrigatória.

Na versão 1.0.0, a categoria é representada como texto. A transformação para enum está planejada para a Fase 2.

**Estado:** obrigatoriedade vigente no MVP; enum planejado para a Fase 2.

#### RN13 - Ano de publicação obrigatório

O ano de publicação deve ser informado no cadastro.

A ausência de `anoDePublicacao` deve ser rejeitada como erro de entrada antes da persistência. O refinamento desse comportamento, para garantir resposta HTTP 400 em todos os payloads inválidos, está incluído na Fase 2.

**Estado:** regra de negócio vigente; robustez do tratamento planejada para a Fase 2.

#### RN14 - Ano de publicação não futuro

O ano de publicação não pode ser posterior ao ano atual.

**Estado:** vigente no MVP.

#### RN15 - Proprietário obrigatório

Todo livro deve estar associado a um proprietário.

**Estado:** vigente no MVP.

#### RN16 - Proprietário existente

O proprietário informado no cadastro do livro deve existir previamente no sistema.

A API recebe o identificador do proprietário e busca a entidade correspondente antes de persistir o livro.

**Estado:** vigente no MVP.

### 2.2 Atualização

#### RN17 - Proprietário imutável no livro

Não é permitido alterar o proprietário de um livro depois da criação.

A restrição é estrutural: o contrato de atualização do livro não contém o identificador do proprietário.

**Estado:** vigente no MVP.

#### RN18 - Validações na atualização

A atualização do livro deve respeitar as regras aplicáveis a título, autor, categoria e ano de publicação.

**Estado:** vigente no MVP.

### 2.3 Exclusão

#### RN19 - Exclusão permitida

Um livro pode ser excluído a qualquer momento, desde que o registro exista.

**Estado:** vigente no MVP.

#### RN20 - Exclusão não afeta o proprietário

A exclusão de um livro não deve excluir nem alterar o proprietário associado.

**Estado:** vigente no MVP.

---

## 3. Consultas e listagens

### RN21 - Lista vazia é resultado válido

Consultas de livros e proprietários podem retornar uma lista vazia quando não existirem registros correspondentes. Lista vazia não representa erro de servidor.

**Estado:** vigente no MVP.

### RN22 - Busca por categoria

A busca por categoria deve retornar somente os livros pertencentes à categoria informada.

O comportamento da rota quando a categoria não é informada e a possível migração de path parameter para query parameter serão revisados na Fase 2.

**Estado:** busca vigente no MVP; revisão do contrato planejada para a Fase 2.

### RN23 - Busca por proprietário

A busca por proprietário deve retornar somente os livros associados ao identificador informado.

Quando não houver livros correspondentes, o resultado pode ser uma lista vazia.

**Estado:** vigente no MVP.

### RN24 - Paginação

As listagens deverão oferecer paginação para evitar o retorno irrestrito de registros à medida que a base crescer.

**Estado:** planejada para a Fase 2.

---

## 4. Validação e respostas HTTP

### RN25 - Validação antes da persistência

Campos obrigatórios e formatos de entrada devem ser validados antes da execução da persistência sempre que a regra puder ser verificada na camada da API.

Regras dependentes do estado do banco, como unicidade de CPF e existência de proprietário, devem ser verificadas na camada de serviço e reforçadas pelas restrições do banco.

**Estado:** vigente no MVP, com refinamentos planejados para a Fase 2.

### RN26 - Mensagens de erro

As respostas de erro devem utilizar mensagens claras e suficientes para que o cliente identifique a causa do problema, sem expor detalhes internos desnecessários.

A padronização do corpo das respostas e a adoção de exceções customizadas estão planejadas para a Fase 2.

**Estado:** comportamento básico vigente; padronização planejada para a Fase 2.

### RN27 - Códigos HTTP

A API deve usar códigos HTTP coerentes com o resultado da operação, incluindo:

- `200 OK` para consultas e atualizações concluídas;
- `201 Created` para inclusões concluídas;
- `204 No Content` para exclusões concluídas;
- `400 Bad Request` para payloads ou campos inválidos;
- `404 Not Found` para recursos inexistentes;
- `409 Conflict` para conflitos de negócio, como CPF duplicado ou futura tentativa de excluir proprietário com livros;
- `500 Internal Server Error` somente para falhas internas não previstas, e não como substituto de validação de entrada ou conflito de negócio.

**Estado:** vigente no MVP, com refinamentos planejados para a Fase 2.

---

## 5. Integridade e identidade

### RN28 - Identidade do proprietário

O CPF é a chave de negócio imutável do proprietário. O identificador numérico gerado pelo banco permanece como chave técnica de persistência.

**Estado:** vigente no MVP.

### RN29 - Identidade do livro

O UUID é a chave de negócio imutável do livro. O identificador numérico gerado pelo banco permanece como chave técnica de persistência.

**Estado:** vigente no MVP.

### RN30 - Integridade referencial

Todo livro persistido deve possuir referência válida a um proprietário existente. A aplicação e o banco devem preservar essa relação.

**Estado:** vigente no MVP.

---

## 6. Resumo por fase

### Fase 1 - MVP concluído

- Cadastro, consulta, atualização e exclusão de Proprietário.
- Cadastro, consulta, atualização e exclusão de Livro.
- CPF único e imutável.
- UUID como chave de negócio do Livro.
- Proprietário obrigatório e existente para o cadastro de Livro.
- Proprietário do Livro imutável após a criação.
- Validações básicas de campos.
- Consultas por categoria e proprietário.
- Respostas HTTP essenciais.

### Fase 2 - Melhorias funcionais e de qualidade

- Implementar o bloqueio de exclusão de proprietário com livros associados.
- Adicionar validação algorítmica de CPF.
- Implementar paginação.
- Transformar categoria em enum.
- Revisar a busca por categoria e seu contrato de rota.
- Rejeitar de forma padronizada payloads sem `anoDePublicacao`.
- Padronizar respostas de erro e exceções customizadas.
- Isolar o banco de testes.

---

[Voltar ao README](../README.md)
