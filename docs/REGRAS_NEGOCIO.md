# 📋 Regras de Negócio

## Proprietário

### Cadastro

- **RN01:** Todo proprietário deve ter um nome único
- **RN02:** O nome é obrigatório
- **RN03:** O nome deve ter no mínimo 3 caracteres

### Exclusão

- **RN04:** Um proprietário só pode ser excluído se não tiver livros associados

---

## Livro

### Cadastro

- **RN05:** Livro sem título é inválido
- **RN06:** Livro sem proprietário é inválido
- **RN07:** O título deve possuir ao menos 3 caracteres
- **RN08:** O proprietário deve existir antes do cadastro do livro
- **RN09:** Categoria é obrigatória
- **RN10:** Ano de publicação deve ser válido (não pode ser no futuro)
- **RN11:** Autor é obrigatório

### Atualização

- **RN12:** Não é permitido alterar o proprietário de um livro após criação
- **RN13:** As mesmas validações de cadastro se aplicam à atualização

### Exclusão

- **RN14:** Um livro pode ser excluído a qualquer momento
- **RN15:** A exclusão de um livro não afeta o proprietário

---

## Validações Gerais

- **RN16:** Todos os campos obrigatórios devem ser validados antes de persistir
- **RN17:** Mensagens de erro devem ser claras e informativas
- **RN18:** Códigos HTTP apropriados devem ser retornados (400, 404, 500)

---

[Voltar ao README](../README.md)