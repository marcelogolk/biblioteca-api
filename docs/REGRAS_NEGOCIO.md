# 📋 Regras de Negócio

## Proprietário

### Cadastro

- **RN02:** O nome é obrigatório
- **RN03:** O nome deve ter no mínimo 3 caracteres
- **RN04:** O CPF é obrigatório, devendo ser composto por exatamente 11 dígitos numéricos (validação de formato via Expressão Regular; validação algorítmica dos dígitos verificadores fica adiada para a Fase 2)
- **RN05:** O CPF deve ser único no sistema (não pode haver dois proprietários com o mesmo CPF)

### Atualização

- **RN06:** O CPF atua como chave de negócio imutável da entidade. Não existe nenhum caminho na API para alterar o CPF de um proprietário já cadastrado — o contrato de entrada da atualização (`ProprietarioAtualizacaoDTO`) não possui o campo `cpf`. Caso um CPF tenha sido cadastrado incorretamente, a única correção possível é excluir o proprietário (`DELETE`) e cadastrar um novo (`POST`).

### Exclusão

- **RN07:** Um proprietário só pode ser excluído se não tiver livros associados *(pendente de implementação — ver pendências na Fase 1)*

---

## Livro

### Cadastro

- **RN08:** Livro sem título é inválido
- **RN09:** Livro sem proprietário é inválido
- **RN10:** O título deve possuir ao menos 3 caracteres
- **RN11:** O proprietário deve existir antes do cadastro do livro
- **RN12:** Categoria é obrigatória
- **RN13:** Ano de publicação deve ser válido (não pode ser no futuro)
- **RN14:** Autor é obrigatório

### Atualização

- **RN15:** Não é permitido alterar o proprietário de um livro após criação
- **RN16:** As mesmas validações de cadastro se aplicam à atualização

### Exclusão

- **RN17:** Um livro pode ser excluído a qualquer momento
- **RN18:** A exclusão de um livro não afeta o proprietário

---

## Validações Gerais

- **RN19:** Todos os campos obrigatórios devem ser validados antes de persistir
- **RN20:** Mensagens de erro devem ser claras e informativas
- **RN21:** Códigos HTTP apropriados devem ser retornados (400, 404, 409, 500)

---

[Voltar ao README](../README.md)