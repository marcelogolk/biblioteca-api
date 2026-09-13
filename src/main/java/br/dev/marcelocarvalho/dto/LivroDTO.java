package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Livro;
import br.dev.marcelocarvalho.entity.Proprietario;

public record LivroDTO(Long id,
                       String titulo,
                       String autor,
                       String categoria,
                       int anoDePublicacao,
                       Proprietario proprietario) {

    public LivroDTO (Livro entity){
        this(entity.getId(),
                entity.getTitulo(),
                entity.getAutor(),
                entity.getCategoria(),
                entity.getAnoDePublicacao(),
                entity.getProprietario());
    }
}