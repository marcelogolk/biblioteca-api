package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Livro;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Year;

public record LivroDTO(
        Long id,
        String uuid,
        String titulo,
        String autor,
        @QueryParam("categoria")
        @Pattern(
                regexp = "(?s)(?=.*\\S).*",
                message = "Categoria não pode ser vazia ou conter apenas espaços."
        )
        String categoria,
        @Schema(type = SchemaType.INTEGER, example = "2026", description = "Ano de publicação do livro")
        Year anoDePublicacao,
        Long proprietarioId
) {
    public LivroDTO(Livro livro) {
        this(
                livro.getId(),
                livro.getUuid(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getCategoria(),
                livro.getAnoDePublicacao(),
                livro.getProprietario().id
        );
    }
}