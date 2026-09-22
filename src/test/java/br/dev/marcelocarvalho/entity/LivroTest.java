package br.dev.marcelocarvalho.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Year;
import static org.junit.jupiter.api.Assertions.*;

class LivroTest {
    private Livro primeiroLivro;
    private Livro segundoLivro;
    private Livro terceiroLivro;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        primeiroLivro = new Livro("Titulo", "Primeiro Autor", "Categoria", Year.of(2026), new Proprietario("Proprietario", "01234567890"));
        segundoLivro = new Livro("Segundo Titulo", "Segundo Autor", "Categoria", Year.of(2025), new Proprietario("Proprietario", "01234567890"));
        terceiroLivro = new Livro("Terceiro Titulo", "Terceiro Autor", "Categoria", Year.of(2024), new Proprietario("Proprietario", "01234567890"));

        // Força o mesmo UUID via Reflection para o teste isolado
        Field uuidField = Livro.class.getDeclaredField("uuid");
        uuidField.setAccessible(true);
        uuidField.set(segundoLivro, primeiroLivro.getUuid());
    }

    @Test
    @DisplayName("Reflexividade: o objeto deve ser igual a ele mesmo")
    void testEquals_ReflexividadeDeveRetornarTrue() {
        assertEquals(primeiroLivro, primeiroLivro);
    }

    @Test
    @DisplayName("Simetria: se proprietario.equals(segundoProprietario) é true, segundoPropriertario.equals(proprietario) também deve ser true")
    void testEquals_SimetriaDeveRetornarTrue() {
        assertEquals(primeiroLivro,segundoLivro);
        assertEquals(segundoLivro, primeiroLivro);
    }

    @Test
    @DisplayName("Livros diferentes deve retornar falso")
    void testEquals_DiferentesLivrosDeveretornarFalse() {
        assertNotEquals(primeiroLivro, terceiroLivro);

    }

    @Test
    @DisplayName("livro coparado com null deve retornar falso")
    void testEquals_LivroComparadoComNullDeveretornarFalse() {
        assertNotEquals(null, primeiroLivro);

    }

    @Test
    @DisplayName("Livros iguais devem ter o mesmo HashCode")
    void testHashCode_ProprietariosIguaisDevemRetornarTreu() {
        assertEquals(primeiroLivro.hashCode(), primeiroLivro.hashCode());
    }

    @Test
    @DisplayName("proprietarios com o cpf iguais devem ter o mesmo HashCode")
    void testHashCode_ProprietariosComCpfIguaisDevemRetornarTrue() {
        assertEquals(primeiroLivro.hashCode(), segundoLivro.hashCode());
    }

    @Test
    @DisplayName("proprietarios com o cpf diferentes não devem ter o HashCode diferente")
    void testHashCode_ProprietariosComCpfDiferentesDevemRetornarFalse() {
        assertNotEquals(primeiroLivro.hashCode(), terceiroLivro.hashCode());
    }
}