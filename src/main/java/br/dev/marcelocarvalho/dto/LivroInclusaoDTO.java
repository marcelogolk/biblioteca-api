package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Proprietario;

public record LivroInclusaoDTO(String titulo,
                               String autor,
                               String categoria,
                               int anoDePublicacao,
                               Proprietario proprietario) {
}
