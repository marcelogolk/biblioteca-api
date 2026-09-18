package br.dev.marcelocarvalho.dto;

import br.dev.marcelocarvalho.entity.Proprietario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProprietarioDTO(
        Long id,
        String nome,
        String cpf) {

    public ProprietarioDTO(Proprietario entity) {
        this(entity.getId(),
             entity.getNome(),
             entity.getCpf());
    }
}
