package br.dev.marcelocarvalho.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class LivroControllerIT {

    private Long proprietarioId;
    private Long livroId;

    @BeforeEach
    void criarProprietarioELivro() {
        String cpfUnico = String.valueOf(System.currentTimeMillis()).substring(0, 11);

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

        livroId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Titulo Original", "autor": "Autor Original", "categoria": "Categoria", "anoDePublicacao": 2020, "proprietarioId": %d}
                        """.formatted(proprietarioId))
                .when()
                .post("/api/livros")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @AfterEach
    void limparDados() {
        given().when().delete("/api/livros/{id}", livroId);
        given().when().delete("/api/proprietarios/{id}", proprietarioId);
    }

    @Test
    void testAtualizarLivro_deveRetornarLivroAtualizadoComIdEUuid() {
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
}