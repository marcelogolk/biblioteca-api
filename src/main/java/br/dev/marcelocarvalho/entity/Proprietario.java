package br.dev.marcelocarvalho.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;
@Entity
@Table(name = "proprietarios")
public class Proprietario extends PanacheEntity {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, message = "Nome deve ter no mínimo 3 caracteres")
    @Column(unique = true, nullable = false)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Column(unique = true)
    public String cpf;

    public Proprietario() {
    }

    public Proprietario(Long id, String nome, String cpf) {
     //   this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() { return cpf; }

    public void setCpf(String cpf) { this.cpf = cpf; }

    @Override
    public int hashCode() {
        return Objects.hashCode(cpf);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Proprietario proprietario)) return false;
        return Objects.equals(cpf, proprietario.cpf);
    }

    @Override
    public String toString() {
        return "Proprietario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}