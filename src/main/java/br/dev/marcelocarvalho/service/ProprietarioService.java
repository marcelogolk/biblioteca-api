package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.ProprietarioDTO;
import br.dev.marcelocarvalho.dto.ProprietarioInclusaoDTO;
import br.dev.marcelocarvalho.entity.Proprietario;
import br.dev.marcelocarvalho.repository.ProprietarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProprietarioService {
    @Inject
    ProprietarioRepository proprietarioRepository;

    @Transactional
    public ProprietarioDTO incluirProprietario(ProprietarioInclusaoDTO proprietarioInclusaoDTO){
        Proprietario proprietarioEntity = new Proprietario();
        proprietarioEntity.setNome(proprietarioInclusaoDTO.nome());
        proprietarioRepository.persist(proprietarioEntity);
        return new ProprietarioDTO(proprietarioEntity);
    }
}
