package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.*;
import br.dev.marcelocarvalho.entity.Livro;
import br.dev.marcelocarvalho.entity.Proprietario;
import br.dev.marcelocarvalho.repository.LivroRepository;
import br.dev.marcelocarvalho.repository.ProprietarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class LivroService {

    @Inject
    LivroRepository livroRepository;

    @Inject
    ProprietarioRepository proprietarioRepository;

    public List<LivroDTO> listAllLivros() {
        return livroRepository.listAll()
                .stream()
                .map(LivroDTO::new)
                .toList();
    }

    public LivroDTO buscarLivroById(Long id){
        Livro livro = livroRepository.findById(id);
        if (livro != null){
            return new LivroDTO(livro);
        } else {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND) // 404
                            .entity("Não encontrado Livro com o id:"+ id)
                            .build()
            );
        }
    }

    @Transactional
    public LivroDTO incluirLivro(@Valid LivroInclusaoDTO livroInclusaoDTO){
        Proprietario proprietario = proprietarioRepository
                .findByIdOptional(livroInclusaoDTO.proprietarioId())
                .orElseThrow(
                        () -> new WebApplicationException(
                                Response
                                        .status(Response.Status.NOT_FOUND)
                                        .entity("Livro precisa de ter um Proprietário cadastrado para ser incluido")
                                        .build()
                        )
                );

        Livro livroEntity = new Livro(
                livroInclusaoDTO.titulo(),
                livroInclusaoDTO.autor(),
                livroInclusaoDTO.categoria(),
                livroInclusaoDTO.anoDePublicacao(),
                proprietario);

        if (isUuidJaExiste(livroEntity.getUuid())) {
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .entity("Ocorreu um conflito na geração do identificador único do livro. Tente novamente.")
                            .build()
            );
        }
        livroRepository.persist(livroEntity);
        return new LivroDTO(livroEntity);
    }

    @Transactional
    public LivroDTO atualizarLivro(Long id, @Valid LivroAtualizacaoDTO livroAtualizacaoDTO) {
        Livro livro = livroRepository
                .findByIdOptional(id)
                .orElseThrow(
                        () -> new WebApplicationException(
                                Response
                                        .status(Response.Status.NOT_FOUND)
                                        .entity("Não encontrado Livro com o id:" + id)
                                        .build()
                        )
                );
        livro.setTitulo(livroAtualizacaoDTO.titulo());
        livro.setAutor(livroAtualizacaoDTO.autor());
        livro.setCategoria(livroAtualizacaoDTO.categoria());
        livro.setAnoDePublicacao(livroAtualizacaoDTO.anoDePublicacao());
        return new LivroDTO(livro);
    }

    @Transactional
    public void deletarLivroById(Long id) {
        if (!livroRepository.deleteById(id)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Livro não encontrado")
                            .build()
            );
        }
    }

    private boolean isUuidJaExiste(String uuid) {
        return livroRepository
                .find("uuid", uuid)
                .firstResultOptional()
                .isPresent();
    }
}
