package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.LivroDTO;
import br.dev.marcelocarvalho.dto.LivroInclusaoDTO;
import br.dev.marcelocarvalho.entity.Livro;
import br.dev.marcelocarvalho.repository.LivroRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LivroService {

    @Inject
    LivroRepository livroRepository;

    //String titulo, String autor, String categoria, int anoDePublicacao, Proprietario proprietario
    @Transactional
    public LivroDTO incluirLivro(LivroInclusaoDTO livroInclusaoDTO){
        Livro livroEntity = new Livro();
        livroEntity.setTitulo(livroInclusaoDTO.titulo());
        livroEntity.setAutor(livroInclusaoDTO.autor());
        livroEntity.setCategoria(livroInclusaoDTO.categoria());
        livroEntity.setAnoDePublicacao(livroInclusaoDTO.anoDePublicacao());
        livroEntity.setProprietario(livroInclusaoDTO.proprietario());
        livroRepository.persist(livroEntity);
        return new LivroDTO(livroEntity);
    }
}
