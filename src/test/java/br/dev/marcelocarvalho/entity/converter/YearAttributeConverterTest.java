package br.dev.marcelocarvalho.entity.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class YearAttributeConverterTest {

    private YearAttributeConverter yearAttributeConverter;

    @BeforeEach
     void setup() {
        yearAttributeConverter = new YearAttributeConverter();
    }

    @Test
    @DisplayName("Deve converter Year para Integer com sucesso")
    void convertToDatabaseColumnTest() {
        Year year = java.time.Year.of(2026);
        Integer result= yearAttributeConverter.convertToDatabaseColumn(year);
        assertNotNull(result);
        assertEquals(2026, result);
    }

    @Test
    @DisplayName("Deve retornar null ao converter Year nulo para coluna do banco")
    void convertToDatabaseColumn_quandoNulo_deveRetornarNuloTest() {
        Integer result= yearAttributeConverter.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter Integer do banco para objeto Year com sucesso")
    void convertToEntityAttributeTest() {
        Integer dbDate = 2026;
        Year result = yearAttributeConverter.convertToEntityAttribute(dbDate);
        assertNotNull(result);
        assertEquals(Year.of(2026), result);
    }
    @Test
    @DisplayName("Deve retornar null ao converter Integer nulo para atributo de entidade")
    void convertToEntityAttribute_quandoNulo_deveRetornarNuloTest() {
        Year result = yearAttributeConverter.convertToEntityAttribute(null);
         assertNull(result);
    }
}