package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.ProprietarioDTO;
import br.dev.marcelocarvalho.dto.ProprietarioAtualizacaoDTO;
import br.dev.marcelocarvalho.dto.ProprietarioInclusaoDTO;
import br.dev.marcelocarvalho.entity.Proprietario;
import br.dev.marcelocarvalho.repository.ProprietarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
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
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND) // 404
                    .entity("Não encontrado proprietário com o id:"+ id)
                    .build()
            );
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
        if (isCPFJaExiste(proprietarioInclusaoDTO)){
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .entity("Já existe um proprietário com o CPF informado")
                            .build()
            );
        }
        Proprietario proprietarioEntity = new Proprietario(proprietarioInclusaoDTO.nome(),proprietarioInclusaoDTO.cpf());
        proprietarioRepository.persist(proprietarioEntity);
        return new ProprietarioDTO(proprietarioEntity);
    }


    @Transactional
    public ProprietarioDTO atualizarProprietario(Long id , ProprietarioAtualizacaoDTO proprietarioAtualizacaoDTO) {
        Proprietario proprietario = proprietarioRepository.findById(id);
        if (proprietario == null ) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Proprietário não encontrado")
                            .build()
            );
        }
        proprietario.setNome(proprietarioAtualizacaoDTO.nome());
            return new ProprietarioDTO(proprietario);
        }

    @Transactional
    public void deletarProprietarioById(Long id){
        if (!proprietarioRepository.deleteById(id)){
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                    .entity("Proprietário não encontrado")
                    .build()
            );
        }
    }

    private boolean isCPFJaExiste(ProprietarioInclusaoDTO proprietarioInclusaoDTO) {
        return proprietarioRepository
                .find("cpf", proprietarioInclusaoDTO.cpf())
                .firstResultOptional()
                .isPresent();
    }
}