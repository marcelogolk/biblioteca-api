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
            return null;
        }
    }

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
        if (isNomeJaExiste(proprietarioInclusaoDTO)) {
            throw new WebApplicationException("Já existe um proprietário com esse nome", 400);
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
        proprietarioRepository.deleteById(id);
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
