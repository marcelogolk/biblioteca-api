package br.dev.marcelocarvalho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProprietarioInclusaoDTO(@NotBlank(message = "Nome é obrigatório")
                                       @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
                                       String nome,
                                       @NotBlank(message = "CPF é obrigatório")
                                       @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
                                       String cpf) {
}
