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

    public ProprietarioDTO buscarProprietarioById(Long id){
        Proprietario proprietario = proprietarioRepository.findById(id);
        if (proprietario != null){
            return new ProprietarioDTO(proprietario);
        } else {
            throw new WebApplicationException("Não encontrado proprietário com o id:"+ id, 404);
        }
    }

    public List<ProprietarioDTO> listAllProprietario() {
        return proprietarioRepository.listAll()
                .stream()
                .map(ProprietarioDTO::new)
                .toList();
    }

    @Transactional
    public ProprietarioDTO incluirProprietario(ProprietarioInclusaoDTO proprietarioInclusaoDTO){
        if (isNomeJaExiste(proprietarioInclusaoDTO)) {
            throw new WebApplicationException("Já existe um proprietário com esse nome", 409);
        }
        if (isCPFJaExiste(proprietarioInclusaoDTO)){
            throw new WebApplicationException("Já existe um proprietário com o novo CPF informado", 409);
        }
        Proprietario proprietarioEntity = new Proprietario(proprietarioInclusaoDTO.nome(),proprietarioInclusaoDTO.cpf());
        proprietarioRepository.persist(proprietarioEntity);
        return new ProprietarioDTO(proprietarioEntity);
    }


    @Transactional
    public ProprietarioDTO atualizarProprietario(Long id , ProprietarioInclusaoDTO proprietarioInclusaoDTO) {
        Proprietario proprietario = proprietarioRepository.findById(id);
        if (proprietario == null ) {
            throw new WebApplicationException("Proprietário não encontrado", 404);
        }
        if (proprietario.getCpf().equals(proprietarioInclusaoDTO.cpf())){
            proprietario.setNome(proprietarioInclusaoDTO.nome());
            proprietarioRepository.persist(proprietario);
            return new ProprietarioDTO(proprietario);
        } else {
            if (isCPFJaExiste(proprietarioInclusaoDTO)){
                throw new WebApplicationException("Já existe um proprietário com o novo CPF informado", 409);
            }
            proprietarioRepository.delete(proprietario);
            proprietarioRepository.flush();
            Proprietario novoProprietario = new Proprietario(
                    proprietarioInclusaoDTO.nome(),
                    proprietarioInclusaoDTO.cpf());
            proprietarioRepository.persist(novoProprietario);
            return new ProprietarioDTO(novoProprietario);
        }
    }

    @Transactional
    public void deletarProprietarioById(Long id){
        if (!proprietarioRepository.deleteById(id)){
            throw new WebApplicationException("Proprietário não encontrado", 404);
        }
    }


    private boolean isNomeJaExiste(ProprietarioInclusaoDTO proprietarioInclusaoDTO) {
        return proprietarioRepository
                .find("nome", proprietarioInclusaoDTO.nome())
                .firstResultOptional()
                .isPresent();
    }

    private boolean isCPFJaExiste(ProprietarioInclusaoDTO proprietarioInclusaoDTO) {
        return proprietarioRepository
                .find("cpf", proprietarioInclusaoDTO.cpf())
                .firstResultOptional()
                .isPresent();
    }

}
