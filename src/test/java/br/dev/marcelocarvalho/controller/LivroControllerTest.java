package br.dev.marcelocarvalho.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class LivroControllerTest {

    private static final String PLACEHOLDER_PROPRIETARIO_ID = "__PROPRIETARIO_ID__";

    private Long proprietarioId;
    private final List<Long> livrosCriados = new ArrayList<>();

    @BeforeEach
    void criarProprietario() {
        String cpfUnico = String.valueOf(System.nanoTime()).substring(0, 11);

        proprietarioId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Proprietario Teste", "cpf": "%s"}
                        """.formatted(cpfUnico))
                .when()
                .post("/api/proprietarios")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @AfterEach
    void limparDados() {
        for (Long livroId : livrosCriados) {
            given().when().delete("/api/livros/{id}", livroId);
        }
        livrosCriados.clear();
        given().when().delete("/api/proprietarios/{id}", proprietarioId);
    }

    // ---------- Atualização ----------

    @Test
    void testAtualizarLivro_deveRetornarLivroAtualizadoComIdEUuid() {
        Long livroId = criarLivro("Titulo Original", "Autor Original", "Categoria", 2020, proprietarioId);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Titulo Atualizado", "autor": "Autor Atualizado", "categoria": "Categoria Nova", "anoDePublicacao": 2021}
                        """)
                .when()
                .put("/api/livros/{id}", livroId)
                .then()
                .statusCode(200)
                .body("id", equalTo(livroId.intValue()))
                .body("uuid", notNullValue())
                .body("titulo", equalTo("Titulo Atualizado"));
    }

    // ---------- Inclusão ----------

    static Stream<Arguments> cenariosIncluirLivro() {
        int anoAtual = Year.now().getValue();
        int anoFuturo = anoAtual + 1;

        return Stream.of(
                Arguments.of("Caminho feliz",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        201),

                Arguments.of("RN08: sem titulo",
                        """
                        {"autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        400),

                Arguments.of("RN09: sem proprietarioId",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d}
                        """.formatted(anoAtual),
                        400),

                Arguments.of("RN10: titulo com 2 caracteres",
                        """
                        {"titulo": "ab", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        400),

                Arguments.of("RN10: titulo com exatamente 3 caracteres",
                        """
                        {"titulo": "abc", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        201),

                Arguments.of("RN12: sem categoria",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        400),

                Arguments.of("RN13: ano no futuro",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoFuturo, PLACEHOLDER_PROPRIETARIO_ID),
                        400),

                Arguments.of("RN13: ano igual ao atual",
                        """
                        {"titulo": "Titulo Valido", "autor": "Autor Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        201),

                Arguments.of("RN14: sem autor",
                        """
                        {"titulo": "Titulo Valido", "categoria": "Categoria", "anoDePublicacao": %d, "proprietarioId": %s}
                        """.formatted(anoAtual, PLACEHOLDER_PROPRIETARIO_ID),
                        400)
        );
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("cenariosIncluirLivro")
    void incluirLivro_cenarios(String descricao, String corpoJsonComPlaceholder, int statusEsperado) {
        String corpoJson = corpoJsonComPlaceholder.replace(PLACEHOLDER_PROPRIETARIO_ID, String.valueOf(proprietarioId));

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

    @Test
    void excluirLivro_deveRetornar204EConfirmarRemocao() {
        Long livroId = criarLivro("Titulo Para Excluir", "Autor", "Categoria", 2020, proprietarioId);

        given()
                .when()
                .delete("/api/livros/{id}", livroId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/api/livros/{id}", livroId)
                .then()
                .statusCode(404);

        livrosCriados.remove(livroId);
    }

    @Test
    void excluirLivro_idNaoExiste_deveRetornar404() {
        given()
                .when()
                .delete("/api/livros/{id}", 999999999L)
                .then()
                .statusCode(404);
    }
    @Test
    void listAllLivros_deveIncluirLivrosRecemCriados() {
        int quantidadeAntes = given()
                .when()
                .get("/api/livros")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("$").size();

        criarLivro("Livro Um", "Autor Um", "Categoria", 2020, proprietarioId);
        criarLivro("Livro Dois", "Autor Dois", "Categoria", 2021, proprietarioId);

        given()
                .when()
                .get("/api/livros")
                .then()
                .statusCode(200)
                .body("size()", equalTo(quantidadeAntes + 2));
    }


    private Long criarLivro(String titulo, String autor, String categoria, int ano, Long proprietarioId) {
        Long livroId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "%s", "autor": "%s", "categoria": "%s", "anoDePublicacao": %d, "proprietarioId": %d}
                        """.formatted(titulo, autor, categoria, ano, proprietarioId))
                .when()
                .post("/api/livros")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        livrosCriados.add(livroId);
        return livroId;
    }
}