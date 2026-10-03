package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.LivroAtualizacaoDTO;
import br.dev.marcelocarvalho.dto.LivroDTO;
import br.dev.marcelocarvalho.dto.LivroInclusaoDTO;
import br.dev.marcelocarvalho.entity.Livro;
import br.dev.marcelocarvalho.entity.Proprietario;
import br.dev.marcelocarvalho.repository.LivroRepository;
import br.dev.marcelocarvalho.repository.ProprietarioRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import java.time.Year;
import java.util.List;

import static org.mockito.Mockito.when;

@QuarkusTest
class LivroServiceTest {

    @Inject
    LivroService livroService;

    @InjectMock
    LivroRepository livroRepository;

    @InjectMock
    ProprietarioRepository proprietarioRepository;

    @Test
    void listAllLivros_deveRetornarListaDeDTOs_quandoExistiremLivros() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        Livro livro1 = new Livro("Livro A", "Autor A", "Ficção", Year.of(2020), proprietario);
        livro1.id = 1L;
        Livro livro2 = new Livro("Livro B", "Autor B", "Romance", Year.of(2021), proprietario);
        livro2.id = 2L;

        when(livroRepository.listAll()).thenReturn(List.of(livro1, livro2));

        List<LivroDTO> resultado = livroService.listAllLivros();

        assertEquals(2, resultado.size());
        assertEquals("Livro A", resultado.get(0).titulo());
        assertEquals("Livro B", resultado.get(1).titulo());
        assertEquals(1L, resultado.get(0).proprietarioId());
    }

    @Test
    void listAllLivros_deveRetornarListaVazia_quandoNaoExistiremLivros() {
        when(livroRepository.listAll()).thenReturn(List.of());

        List<LivroDTO> resultado = livroService.listAllLivros();

        assertEquals(0, resultado.size());
    }

    @Test
    void buscarLivroById_deveRetornarDTO_quandoIdExistente() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        Livro livro = new Livro("Livro A", "Autor A", "Ficção", Year.of(2020), proprietario);
        livro.id = 1L;

        when(livroRepository.findById(1L)).thenReturn(livro);

        LivroDTO resultado = livroService.buscarLivroById(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Livro A", resultado.titulo());
        assertEquals(1L, resultado.proprietarioId());
    }

    @Test
    void buscarLivroById_deveLancar404_quandoIdInexistente() {
        when(livroRepository.findById(99L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> livroService.buscarLivroById(99L)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void buscarLivroByCategoria_deveRetornarListaDeDTOs_quandoCategoriaExistente() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        Livro livro = new Livro("Livro A", "Autor A", "Ficção", Year.of(2020), proprietario);
        livro.id = 1L;

        when(livroRepository.findByCategoria("Ficção")).thenReturn(List.of(livro));

        List<LivroDTO> resultado = livroService.buscarLivroByCategoria("Ficção");

        assertEquals(1, resultado.size());
        assertEquals("Livro A", resultado.get(0).titulo());
        assertEquals("Ficção", resultado.get(0).categoria());
    }

    @Test
    void buscarLivroByCategoria_deveRetornarListaVazia_quandoCategoriaInexistente() {
        when(livroRepository.findByCategoria("Inexistente")).thenReturn(List.of());

        List<LivroDTO> resultado = livroService.buscarLivroByCategoria("Inexistente");

        assertEquals(0, resultado.size());
    }

    @Test
    void buscarLivroByProprietarioId_deveRetornarListaDeDTOs_quandoProprietarioPossuiLivros() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        Livro livro = new Livro("Livro A", "Autor A", "Ficção", Year.of(2020), proprietario);
        livro.id = 1L;

        when(livroRepository.findByProprietarioId(1L)).thenReturn(List.of(livro));

        List<LivroDTO> resultado = livroService.buscarLivroByProprietarioId(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).proprietarioId());
    }

    @Test
    void buscarLivroByProprietarioId_deveRetornarListaVazia_quandoProprietarioNaoPossuiLivros() {
        when(livroRepository.findByProprietarioId(99L)).thenReturn(List.of());

        List<LivroDTO> resultado = livroService.buscarLivroByProprietarioId(99L);

        assertEquals(0, resultado.size());
    }

    @Test
    void incluirLivro_deveCriarLivro_quandoProprietarioExisteEUuidNaoConflita() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        LivroInclusaoDTO dto = new LivroInclusaoDTO(
                "Livro A", "Autor A", "Ficção", Year.of(2020), 1L
        );

        when(proprietarioRepository.findByIdOptional(1L)).thenReturn(Optional.of(proprietario));

        @SuppressWarnings("unchecked")
        PanacheQuery<Livro> panacheQueryMock = mock(PanacheQuery.class);
        when(livroRepository.find(eq("uuid"), any(Object.class))).thenReturn(panacheQueryMock);
        when(panacheQueryMock.firstResultOptional()).thenReturn(Optional.empty());

        LivroDTO resultado = livroService.incluirLivro(dto);

        assertEquals("Livro A", resultado.titulo());
        assertEquals(1L, resultado.proprietarioId());
        verify(livroRepository, times(1)).persist(any(Livro.class));
    }

    @Test
    void incluirLivro_deveLancar404_quandoProprietarioInexistente() {
        LivroInclusaoDTO dto = new LivroInclusaoDTO(
                "Livro A", "Autor A", "Ficção", Year.of(2020), 99L
        );

        when(proprietarioRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> livroService.incluirLivro(dto)
        );

        assertEquals(404, exception.getResponse().getStatus());
        verify(livroRepository, never()).persist(any(Livro.class));
    }

    @Test
    void incluirLivro_deveLancar409_quandoUuidJaExiste() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        LivroInclusaoDTO dto = new LivroInclusaoDTO(
                "Livro A", "Autor A", "Ficção", Year.of(2020), 1L
        );

        Livro livroExistente = new Livro("Outro", "Outro Autor", "Outra", Year.of(2019), proprietario);

        when(proprietarioRepository.findByIdOptional(1L)).thenReturn(Optional.of(proprietario));

        @SuppressWarnings("unchecked")
        PanacheQuery<Livro> panacheQueryMock = mock(PanacheQuery.class);
        when(livroRepository.find(eq("uuid"), any(Object.class))).thenReturn(panacheQueryMock);
        when(panacheQueryMock.firstResultOptional()).thenReturn(Optional.of(livroExistente));
        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> livroService.incluirLivro(dto)
        );

        assertEquals(409, exception.getResponse().getStatus());
        verify(livroRepository, never()).persist(any(Livro.class));
    }

    @Test
    void atualizarLivro_deveAtualizarCampos_quandoIdExistente() {
        Proprietario proprietario = new Proprietario("Dono Teste", "11111111111");
        proprietario.id = 1L;

        Livro livroExistente = new Livro("Título Antigo", "Autor Antigo", "Categoria Antiga", Year.of(2010), proprietario);
        livroExistente.id = 1L;

        LivroAtualizacaoDTO dto = new LivroAtualizacaoDTO(
                "Título Novo", "Autor Novo", "Categoria Nova", Year.of(2020)
        );

        when(livroRepository.findByIdOptional(1L)).thenReturn(Optional.of(livroExistente));

        LivroDTO resultado = livroService.atualizarLivro(1L, dto);

        assertEquals("Título Novo", resultado.titulo());
        assertEquals("Autor Novo", resultado.autor());
        assertEquals("Categoria Nova", resultado.categoria());
        assertEquals(Year.of(2020), resultado.anoDePublicacao());
    }

    @Test
    void atualizarLivro_deveLancar404_quandoIdInexistente() {
        LivroAtualizacaoDTO dto = new LivroAtualizacaoDTO(
                "Título Novo", "Autor Novo", "Categoria Nova", Year.of(2020)
        );

        when(livroRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> livroService.atualizarLivro(99L, dto)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void deletarLivroById_deveExecutarComSucesso_quandoIdExistente() {
        when(livroRepository.deleteById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> livroService.deletarLivroById(1L));

        verify(livroRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletarLivroById_deveLancar404_quandoIdInexistente() {
        when(livroRepository.deleteById(99L)).thenReturn(false);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> livroService.deletarLivroById(99L)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

}