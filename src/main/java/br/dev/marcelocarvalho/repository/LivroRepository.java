package br.dev.marcelocarvalho.repository;

import br.dev.marcelocarvalho.entity.Livro;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LivroRepository implements PanacheRepository<Livro> {
}
