package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.ProprietarioDTO;
import br.dev.marcelocarvalho.dto.ProprietarioInclusaoDTO;
import br.dev.marcelocarvalho.entity.Proprietario;
import br.dev.marcelocarvalho.repository.ProprietarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProprietarioService {
    @Inject
    ProprietarioRepository proprietarioRepository;

    public List<ProprietarioDTO> listAllProprietario() {
        List<ProprietarioDTO> listProprietariosDTO = new ArrayList<ProprietarioDTO>();

        List<Proprietario> listProprietariosEntity = proprietarioRepository.listAll();
        for(Proprietario proprietarioEntity : listProprietariosEntity) {
            listProprietariosDTO.add(new ProprietarioDTO(proprietarioEntity));
        }
        return listProprietariosDTO;
    }

    @Transactional
    public ProprietarioDTO incluirProprietario(ProprietarioInclusaoDTO proprietarioInclusaoDTO){
        boolean nomeJaExiste = proprietarioRepository
                .find("nome", proprietarioInclusaoDTO.nome())
                .firstResultOptional()
                .isPresent();

        if (nomeJaExiste) {
            throw new WebApplicationException("Já existe um proprietário com esse nome", 400);
        }
        Proprietario proprietarioEntity = new Proprietario();
        proprietarioEntity.setNome(proprietarioInclusaoDTO.nome());
        proprietarioEntity.setCpf(proprietarioInclusaoDTO.cpf());
        proprietarioRepository.persist(proprietarioEntity);
        return new ProprietarioDTO(proprietarioEntity);
    }
}
