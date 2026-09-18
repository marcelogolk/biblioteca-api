package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Livro;
import java.time.Year;

public record LivroDTO(
        Long id,
        String uuid,
        String titulo,
        String autor,
        String categoria,
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