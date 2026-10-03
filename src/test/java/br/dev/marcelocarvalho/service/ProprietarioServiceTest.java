package br.dev.marcelocarvalho.service;

import br.dev.marcelocarvalho.dto.ProprietarioAtualizacaoDTO;
import br.dev.marcelocarvalho.dto.ProprietarioDTO;
import br.dev.marcelocarvalho.dto.ProprietarioInclusaoDTO;
import br.dev.marcelocarvalho.entity.Proprietario;
import br.dev.marcelocarvalho.repository.ProprietarioRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ProprietarioServiceTest {

    @Inject
    ProprietarioService proprietarioService;

    @InjectMock
    ProprietarioRepository proprietarioRepository;

    @Test
    void buscarProprietarioById_deveRetornarDTO_quandoIdExistente() {
        Proprietario proprietario = new Proprietario("Maria Teste", "12345678901");
        proprietario.id = 1L;

        when(proprietarioRepository.findById(1L)).thenReturn(proprietario);

        ProprietarioDTO resultado = proprietarioService.buscarProprietarioById(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Maria Teste", resultado.nome());
        assertEquals("12345678901", resultado.cpf());
    }

    @Test
    void buscarProprietarioById_deveLancar404_quandoIdInexistente() {
        when(proprietarioRepository.findById(99L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> proprietarioService.buscarProprietarioById(99L)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void incluirProprietario_deveRetornarDTO_quandoCaminhoFeliz() {
        ProprietarioInclusaoDTO dto = new ProprietarioInclusaoDTO("João Teste", "98765432100");

        PanacheQuery<Proprietario> queryMock = mock(PanacheQuery.class);
        when(proprietarioRepository.find("cpf", dto.cpf())).thenReturn(queryMock);
        when(queryMock.firstResultOptional()).thenReturn(Optional.empty());

        ProprietarioDTO resultado = proprietarioService.incluirProprietario(dto);

        assertEquals("João Teste", resultado.nome());
        assertEquals("98765432100", resultado.cpf());
        verify(proprietarioRepository).persist(any(Proprietario.class));
    }

    @Test
    void incluirProprietario_deveLancar409_quandoCpfJaExiste() {
        ProprietarioInclusaoDTO dto = new ProprietarioInclusaoDTO("João Teste", "98765432100");
        Proprietario existente = new Proprietario("Outro Nome", "98765432100");

        PanacheQuery<Proprietario> queryMock = mock(PanacheQuery.class);
        when(proprietarioRepository.find("cpf", dto.cpf())).thenReturn(queryMock);
        when(queryMock.firstResultOptional()).thenReturn(Optional.of(existente));

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> proprietarioService.incluirProprietario(dto)
        );

        assertEquals(409, exception.getResponse().getStatus());
        verify(proprietarioRepository, never()).persist(any(Proprietario.class));
    }

    @Test
    void atualizarProprietario_deveRetornarDTOAtualizado_quandoIdExistente() {
        Proprietario proprietario = new Proprietario("Nome Antigo", "11122233344");
        proprietario.id = 1L;

        when(proprietarioRepository.findById(1L)).thenReturn(proprietario);

        ProprietarioAtualizacaoDTO dto = new ProprietarioAtualizacaoDTO("Nome Novo");

        ProprietarioDTO resultado = proprietarioService.atualizarProprietario(1L, dto);

        assertEquals(1L, resultado.id());
        assertEquals("Nome Novo", resultado.nome());
        assertEquals("11122233344", resultado.cpf());
    }

    @Test
    void atualizarProprietario_deveLancar404_quandoIdInexistente() {
        when(proprietarioRepository.findById(99L)).thenReturn(null);

        ProprietarioAtualizacaoDTO dto = new ProprietarioAtualizacaoDTO("Qualquer Nome");

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> proprietarioService.atualizarProprietario(99L, dto)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void deletarProprietarioById_naoDeveLancarExcecao_quandoIdExistente() {
        when(proprietarioRepository.deleteById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> proprietarioService.deletarProprietarioById(1L));

        verify(proprietarioRepository).deleteById(1L);
    }

    @Test
    void deletarProprietarioById_deveLancar404_quandoIdInexistente() {
        when(proprietarioRepository.deleteById(99L)).thenReturn(false);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> proprietarioService.deletarProprietarioById(99L)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void listAllProprietario_deveRetornarListaDeDTOs_quandoExistiremProprietarios() {
        Proprietario p1 = new Proprietario("Ana Teste", "11111111111");
        p1.id = 1L;
        Proprietario p2 = new Proprietario("Bruno Teste", "22222222222");
        p2.id = 2L;

        when(proprietarioRepository.listAll()).thenReturn(List.of(p1, p2));

        List<ProprietarioDTO> resultado = proprietarioService.listAllProprietario();

        assertEquals(2, resultado.size());
        assertEquals("Ana Teste", resultado.get(0).nome());
        assertEquals("Bruno Teste", resultado.get(1).nome());
    }

    @Test
    void listAllProprietario_deveRetornarListaVazia_quandoNaoExistiremProprietarios() {
        when(proprietarioRepository.listAll()).thenReturn(List.of());

        List<ProprietarioDTO> resultado = proprietarioService.listAllProprietario();

        assertEquals(0, resultado.size());
    }
}
