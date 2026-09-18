package br.dev.marcelocarvalho.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "livros")
public class Livro extends PanacheEntity {

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid;

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 3, message = "Título deve ter no mínimo 3 caracteres")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    @Column(nullable = false)
    private String autor;

    @NotBlank(message = "Categoria é obrigatória")
    @Column(nullable = false)
    private String categoria;

    @PastOrPresent(message = "O Ano de publicação não pode ser futuro")
    @Column(nullable = false)
    private Year anoDePublicacao;

    @NotNull(message = "Proprietário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Proprietario proprietario;

    public Livro() {
        this.uuid = UUID.randomUUID().toString();
    }

    public Livro(String titulo, String autor, String categoria, Year anoDePublicacao, Proprietario proprietario) {
        this.uuid = UUID.randomUUID().toString();
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.anoDePublicacao = anoDePublicacao;
        this.proprietario = proprietario;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Year getAnoDePublicacao() {
        return anoDePublicacao;
    }

    public void setAnoDePublicacao(Year anoDePublicacao) {
        this.anoDePublicacao = anoDePublicacao;
    }

    public Proprietario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Proprietario proprietario) {
        this.proprietario = proprietario;
    }

   public boolean equals(Object o) {
       if (this == o) return true;
        if (!(o instanceof Livro livro)) return false;
        return Objects.equals(uuid, livro.getUuid());
   }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", categoria='" + categoria + '\'' +
                ", anoDePublicacao=" + anoDePublicacao +
                ", proprietarioId=" + (proprietario != null ? proprietario.id : null) +
                '}';
    }
}