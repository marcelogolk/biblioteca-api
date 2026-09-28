package br.dev.marcelocarvalho.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@QuarkusTest
class LivroControllerIncluirLivroIntegrationTest {

    private static Long proprietarioId;
    private static final List<Long> livrosCriados = new ArrayList<>();

    @BeforeAll
    static void criarProprietario() {
        io.restassured.RestAssured.port = org.eclipse.microprofile.config.ConfigProvider.getConfig()
                .getOptionalValue("quarkus.http.test-port", Integer.class)
                .orElse(8081);

        String cpfUnico = String.valueOf(System.currentTimeMillis()).substring(0, 11);

        proprietarioId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Proprietario Teste Livro", "cpf": "%s"}
                        """.formatted(cpfUnico))
                .when()
                .post("/api/proprietarios")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @AfterAll
    static void limparDados() {
        for (Long livroId : livrosCriados) {
            given().when().delete("/api/livros/{id}", livroId);
        }
        if (proprietarioId != null) {
            given().when().delete("/api/proprietarios/{id}", proprietarioId);
        }
    }

    static Stream<Arguments> cenariosIncluirLivro() {
        int anoAtual = Year.now().getValue();
        int anoFuturo = anoAtual + 1;

        return Stream.of(
                Arguments.of("Caminho feliz",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        201),

                Arguments.of("RN08: sem titulo",
                        """
                        {"autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        400),

                Arguments.of("RN09: sem proprietarioId",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d}
                        """.formatted(anoAtual),
                        400),

                Arguments.of("RN10: titulo com 2 caracteres",
                        """
                        {"titulo": "ab", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        400),

                Arguments.of("RN10: titulo com exatamente 3 caracteres",
                        """
                        {"titulo": "abc", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        201),

                Arguments.of("RN12: sem categoria",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        400),

                Arguments.of("RN13: ano no futuro",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoFuturo, proprietarioId),
                        400),

                Arguments.of("RN13: ano igual ao atual",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        201),

                Arguments.of("RN14: sem autor",
                        """
                        {"titulo": "Titulo Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(anoAtual, proprietarioId),
                        400)
        );
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("cenariosIncluirLivro")
    void incluirLivro_cenarios(String descricao, String corpoJson, int statusEsperado) {
        var response = given()
                .contentType(ContentType.JSON)
                .body(corpoJson)
                .when()
                .post("/api/livros")
                .then()
                .statusCode(statusEsperado)
                .extract().response();

        if (statusEsperado == 201) {
            livrosCriados.add(response.jsonPath().getLong("id"));
        }
    }

    @Test
    void incluirLivro_proprietarioNaoExiste_deveRetornar404() {
        int anoAtual = Year.now().getValue();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": 999999999}
                        """.formatted(anoAtual))
                .when()
                .post("/api/livros")
                .then()
                .statusCode(404);
    }
}