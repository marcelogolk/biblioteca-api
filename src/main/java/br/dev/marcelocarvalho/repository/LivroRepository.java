package br.dev.marcelocarvalho.repository;

import br.dev.marcelocarvalho.entity.Livro;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class LivroRepository implements PanacheRepository<Livro> {
    public List<Livro> findByCategoria(String categoria) {
        return list("categoria", categoria);
    }
    public List<Livro> findByProprietarioId(Long proprietarioId){
        return list("proprietario.id", proprietarioId);
    }
}
