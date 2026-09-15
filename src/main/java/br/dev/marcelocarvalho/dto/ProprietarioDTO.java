package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Proprietario;

public record ProprietarioDTO(Long id, String nome, String cpf) {
    public ProprietarioDTO(Proprietario entity){
        this(entity.getId(), entity.getNome(), entity.getCpf());
    }
}
