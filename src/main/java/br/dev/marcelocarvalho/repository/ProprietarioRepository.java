package br.dev.marcelocarvalho.repository;

import br.dev.marcelocarvalho.entity.Proprietario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProprietarioRepository implements PanacheRepository<Proprietario> {

}
