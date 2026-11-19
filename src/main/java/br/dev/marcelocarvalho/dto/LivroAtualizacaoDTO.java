package br.dev.marcelocarvalho.dto;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Year;

public record LivroAtualizacaoDTO(
        @NotBlank(message = "Título é obrigatório")
        @Size(min = 3, message = "Título deve ter no mínimo 3 caracteres")
        String titulo,

        @NotBlank(message = "Autor é obrigatório")
        String autor,

        @QueryParam("categoria")
        @Pattern(
                regexp = "(?s)(?=.*\\S).*",
                message = "Categoria não pode ser vazia ou conter apenas espaços."
        )
        String categoria,

        @Schema(type = SchemaType.INTEGER, example = "2026", description = "Ano de publicação do livro")
        @PastOrPresent(message = "O Ano de publicação não pode ser futuro")
        Year anoDePublicacao){
}