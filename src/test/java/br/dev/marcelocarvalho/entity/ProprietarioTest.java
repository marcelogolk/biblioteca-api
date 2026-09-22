package br.dev.marcelocarvalho.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProprietarioTest {
    private Proprietario proprietario;
    private Proprietario segundoProprietario;
    private Proprietario terceiroProprietario;

    @BeforeEach
    void setup(){
        String cpfUnico = "01234567890";
        String cpfDiferente = "09876543210";
        proprietario = new Proprietario("primeiro Proprietario", cpfUnico);
        segundoProprietario = new Proprietario("segundo Proprietario", cpfUnico);
        terceiroProprietario = new Proprietario("terceiro Proprietario", cpfDiferente);


    }

    @Test
    @DisplayName("Reflexividade: o objeto deve ser igual a ele mesmo")
    void testEquals_ReflexividadeDeveRetornarTrue() {
        assertEquals(proprietario, proprietario);
    }

    @Test
    @DisplayName("Simetria: se proprietario.equals(segundoProprietario) é true, segundoPropriertario.equals(proprietario) também deve ser true")
    void testEquals_SimetriaDeveRetornarTrue() {
        assertEquals(proprietario,segundoProprietario);
        assertEquals(segundoProprietario, proprietario);
    }

    @Test
    @DisplayName("proprietarios diferentes deve retornar falso")
    void testEquals_DiferentesProprietariosDeveretornarFalse() {
        assertNotEquals(proprietario, terceiroProprietario);

    }

    @Test
    @DisplayName("proprietario coparado com null deve retornar falso")
    void testEquals_ProprietariosComparadoComNullDeveretornarFalse() {
        assertNotEquals(null, proprietario);

    }

    @Test
    @DisplayName("proprietarios iguais devem ter o mesmo HashCode")
    void testHashCode_ProprietariosIguaisDevemRetornarTreu() {
        assertEquals(proprietario.hashCode(), proprietario.hashCode());
    }

    @Test
    @DisplayName("proprietarios com o cpf iguais devem ter o mesmo HashCode")
    void testHashCode_ProprietariosComCpfIguaisDevemRetornarTrue() {
        assertEquals(proprietario.hashCode(), segundoProprietario.hashCode());
    }

    @Test
    @DisplayName("proprietarios com o cpf diferentes não devem ter o HashCode diferente")
    void testHashCode_ProprietariosComCpfDiferentesDevemRetornarFalse() {
        assertNotEquals(proprietario.hashCode(), terceiroProprietario.hashCode());
    }

}