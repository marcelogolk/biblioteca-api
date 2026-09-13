package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Proprietario;

public record LivroInclusaoDTO(Long id,
                               String titulo,
                               String autor,
                               String categoria,
                               int anoDePublicacao,
                               Proprietario proprietario) {
}
