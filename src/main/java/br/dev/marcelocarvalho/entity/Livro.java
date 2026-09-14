package br.dev.marcelocarvalho.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.Objects;
@Entity
@Table(name = "livros")
public class Livro extends PanacheEntity {

////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private int anoDePublicacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Proprietario proprietario;

    public Livro(Long id, String titulo, String autor, String categoria, int anoDePublicacao, Proprietario proprietario) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.anoDePublicacao = anoDePublicacao;
        this.proprietario = proprietario;
    }

    public Livro() {
    }

    public long getId() {
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

    public int getAnoDePublicacao() {
        return anoDePublicacao;
    }

    public void setAnoDePublicacao(int anoDePublicacao) {
        this.anoDePublicacao = anoDePublicacao;
    }

    public Proprietario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Proprietario proprietario) {
        this.proprietario = proprietario;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Livro livro)) return false;
        return id == livro.id && anoDePublicacao == livro.anoDePublicacao && Objects.equals(titulo, livro.titulo) && Objects.equals(autor, livro.autor) && Objects.equals(categoria, livro.categoria) && Objects.equals(proprietario, livro.proprietario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, autor, categoria, anoDePublicacao, proprietario);
    }

    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", categoria='" + categoria + '\'' +
                ", anoDePublicacao=" + anoDePublicacao +
                ", proprietario=" + proprietario +
                '}';
    }
}
