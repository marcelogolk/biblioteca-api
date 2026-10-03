package br.dev.marcelocarvalho.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ProprietarioControllerTest {

    private final List<Long> proprietariosCriados = new ArrayList<>();
    private record ProprietarioCriado(Long id, String cpf) {}
    private static final String PLACEHOLDER_CPF = "__CPF__";

    @AfterEach
    void limparDados() {
        for (Long proprietarioId : proprietariosCriados) {
            given().when().delete("/api/proprietarios/{id}", proprietarioId);
        }
        proprietariosCriados.clear();
    }

    private ProprietarioCriado criarProprietario(String nome) {
        String cpfUnico = String.valueOf(System.nanoTime()).substring(0, 11);

        Long proprietarioId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {"nome": "%s", "cpf": "%s"}
                    """.formatted(nome, cpfUnico))
                .when()
                .post("/api/proprietarios")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        proprietariosCriados.add(proprietarioId);
        return new ProprietarioCriado(proprietarioId, cpfUnico);
    }

    static Stream<Arguments> cenariosIncluirProprietario() {
        return Stream.of(
                Arguments.of("Caminho feliz",
                        """
                        {"nome": "Nome Valido", "cpf": "%s"}
                        """.formatted(PLACEHOLDER_CPF),
                        201),

                Arguments.of("Nome ausente",
                        """
                        {"cpf": "%s"}
                        """.formatted(PLACEHOLDER_CPF),
                        400),

                Arguments.of("Nome com 2 caracteres",
                        """
                        {"nome": "ab", "cpf": "%s"}
                        """.formatted(PLACEHOLDER_CPF),
                        400),

                Arguments.of("Nome com exatamente 3 caracteres",
                        """
                        {"nome": "abc", "cpf": "%s"}
                        """.formatted(PLACEHOLDER_CPF),
                        201),

                Arguments.of("CPF ausente",
                        """
                        {"nome": "Nome Valido"}
                        """,
                        400),

                Arguments.of("CPF com menos de 11 digitos",
                        """
                        {"nome": "Nome Valido", "cpf": "123"}
                        """,
                        400),

                Arguments.of("CPF com mascara",
                        """
                        {"nome": "Nome Valido", "cpf": "123.456.789-01"}
                        """,
                        400)
        );
    }
    static Stream<Arguments> cenariosAtualizarProprietarioValidacao() {
        return Stream.of(
                Arguments.of("Nome ausente",
                        """
                        {}
                        """,
                        400),

                Arguments.of("Nome com 2 caracteres",
                        """
                        {"nome": "ab"}
                        """,
                        400),

                Arguments.of("Nome com exatamente 3 caracteres",
                        """
                        {"nome": "abc"}
                        """,
                        200)
        );
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("cenariosAtualizarProprietarioValidacao")
    void atualizarProprietario_cenariosValidacao(String descricao, String corpoJson, int statusEsperado) {
        ProprietarioCriado proprietario = criarProprietario("Nome Original");

        given()
                .contentType(ContentType.JSON)
                .body(corpoJson)
                .when()
                .put("/api/proprietarios/{id}", proprietario.id())
                .then()
                .statusCode(statusEsperado);
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("cenariosIncluirProprietario")
    void incluirProprietario_cenarios(String descricao, String corpoJsonComPlaceholder, int statusEsperado) {
        String cpfUnico = String.valueOf(System.nanoTime()).substring(0, 11);
        String corpoJson = corpoJsonComPlaceholder.replace(PLACEHOLDER_CPF, cpfUnico);

        var response = given()
                .contentType(ContentType.JSON)
                .body(corpoJson)
                .when()
                .post("/api/proprietarios")
                .then()
                .statusCode(statusEsperado)
                .extract().response();

        if (statusEsperado == 201) {
            proprietariosCriados.add(response.jsonPath().getLong("id"));
        }
    }

    @Test
    void incluirProprietario_cpfJaExistente_deveRetornar409() {
        ProprietarioCriado proprietarioExistente = criarProprietario("Proprietario Existente");

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {"nome": "Outro Nome", "cpf": "%s"}
                    """.formatted(proprietarioExistente.cpf()))
                .when()
                .post("/api/proprietarios")
                .then()
                .statusCode(409);
    }

    @Test
    void listAllProprietarios_deveIncluirProprietariosRecemCriados() {
        int quantidadeAntes = given()
                .when()
                .get("/api/proprietarios")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("$").size();

        criarProprietario("Proprietario Um");
        criarProprietario("Proprietario Dois");

        given()
                .when()
                .get("/api/proprietarios")
                .then()
                .statusCode(200)
                .body("size()", equalTo(quantidadeAntes + 2));
    }

    @Test
    void atualizarProprietario_deveRetornarProprietarioAtualizado_quandoDadosValidos() {
        ProprietarioCriado proprietario = criarProprietario("Nome Original");

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {"nome": "Nome Atualizado"}
                    """)
                .when()
                .put("/api/proprietarios/{id}", proprietario.id())
                .then()
                .statusCode(200)
                .body("id", equalTo(proprietario.id().intValue()))
                .body("nome", equalTo("Nome Atualizado"));
    }

    @Test
    void atualizarProprietario_deveRetornar404_quandoIdInexistente() {
        Long idInexistente = 999_999_999_999_999L;

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {"nome": "Nome Qualquer"}
                    """)
                .when()
                .put("/api/proprietarios/{id}", idInexistente)
                .then()
                .statusCode(404);
    }

    @Test
    void buscarProprietarioById_deveRetornarProprietario_quandoIdExistente() {
        ProprietarioCriado proprietario = criarProprietario("Proprietario Teste");

        given()
                .when()
                .get("/api/proprietarios/{id}", proprietario.id())
                .then()
                .statusCode(200)
                .body("id", equalTo(proprietario.id().intValue()))
                .body("nome", equalTo("Proprietario Teste"))
                .body("cpf", equalTo(proprietario.cpf()));
    }

    @Test
    void buscarProprietarioById_deveRetornar404_quandoIdInexistente() {
        Long idInexistente = 999_999_999_999_999L;

        given()
                .when()
                .get("/api/proprietarios/{id}", idInexistente)
                .then()
                .statusCode(404);
    }

    @Test
    void excluirProprietario_deveRetornar204EConfirmarRemocao() {
        ProprietarioCriado proprietario = criarProprietario("Proprietario Para Excluir");

        given()
                .when()
                .delete("/api/proprietarios/{id}", proprietario.id())
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/api/proprietarios/{id}", proprietario.id())
                .then()
                .statusCode(404);

        proprietariosCriados.remove(proprietario.id());
    }

    @Test
    void excluirProprietario_idNaoExiste_deveRetornar404() {
        given()
                .when()
                .delete("/api/proprietarios/{id}", 999_999_999_999_999L)
                .then()
                .statusCode(404);
    }

}