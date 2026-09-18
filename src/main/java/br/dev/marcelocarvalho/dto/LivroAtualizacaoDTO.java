package br.dev.marcelocarvalho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Year;

public record LivroAtualizacaoDTO(
        @NotBlank(message = "Título é obrigatório")
        @Size(min = 3, message = "Título deve ter no mínimo 3 caracteres")
        String titulo,

        @NotBlank(message = "Autor é obrigatório")
        String autor,

        @NotBlank(message = "Categoria é obrigatória")
        String categoria,

        @Schema(type = SchemaType.INTEGER, example = "2026", description = "Ano de publicação do livro")
        @PastOrPresent(message = "O Ano de publicação não pode ser futuro")
        Year anoDePublicacao){
}