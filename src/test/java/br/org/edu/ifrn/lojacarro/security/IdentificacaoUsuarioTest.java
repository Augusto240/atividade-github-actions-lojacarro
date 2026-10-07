package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.cliente.UsuarioClient;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentificacaoUsuarioTest {

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private IdentificacaoUsuario identificacao;

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void exigirSemHeaderDa401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> identificacao.exigir(null, "cadastrar carros"));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(usuarioClient);
    }

    @Test
    void exigirComUsuarioInexistenteDa401EFicaNaAuditoria() {
        when(usuarioClient.buscarNome(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> identificacao.exigir(9L, "cadastrar carros"));

        assertEquals(401, ex.getStatusCode().value());
        verify(auditoriaService).registrar(eq(AcaoAuditoria.ACESSO_NEGADO), eq("#9"), anyString());
    }

    @Test
    void exigirDevolveNomeEColocaNoMdc() {
        when(usuarioClient.buscarNome(2L)).thenReturn(Optional.of("Maria"));

        assertEquals("Maria (#2)", identificacao.exigir(2L, "cadastrar carros"));
        assertEquals("Maria", MDC.get("usuario"));
    }

    @Test
    void identificarSemHeaderEhAnonimo() {
        assertEquals("anonimo", identificacao.identificar(null));
    }

    @Test
    void identificarDevolveONome() {
        when(usuarioClient.buscarNome(2L)).thenReturn(Optional.of("Maria"));

        assertEquals("Maria (#2)", identificacao.identificar(2L));
    }

    @Test
    void identificarUsuarioQueNaoExiste() {
        when(usuarioClient.buscarNome(9L)).thenReturn(Optional.empty());

        assertEquals("#9 (inexistente)", identificacao.identificar(9L));
    }

    @Test
    void identificarSegueMesmoComAApiForaDoAr() {
        when(usuarioClient.buscarNome(2L))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "fora"));

        assertEquals("#2", identificacao.identificar(2L));
    }
}
