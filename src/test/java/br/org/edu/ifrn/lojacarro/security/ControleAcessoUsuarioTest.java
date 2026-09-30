package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControleAcessoUsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ControleAcessoUsuario controleAcesso;

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void semIdentificacaoDeveDar401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controleAcesso.exigirCargo(null, "cadastrar usuários", Cargo.GERENTE));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(usuarioRepository, auditoriaService);
    }

    @Test
    void usuarioQueNaoExisteDeveDar401() {
        when(usuarioRepository.findById(77L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controleAcesso.exigirCargo(77L, "cadastrar usuários", Cargo.GERENTE));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void vendedorTentandoAcaoDeGerenteDeveDar403EFicarNaAuditoria() {
        Usuario caio = usuario(2L, "Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controleAcesso.exigirCargo(2L, "excluir usuários", Cargo.GERENTE));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("O cargo VENDEDOR não pode excluir usuários", ex.getReason());
        verify(auditoriaService).registrar(eq(AcaoAuditoria.ACESSO_NEGADO), eq("Caio #2 (VENDEDOR)"), anyString());
    }

    @Test
    void gerenteDevePassarEFicarNoMdc() {
        Usuario marta = usuario(1L, "Marta", Cargo.GERENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(marta));

        Usuario operador = controleAcesso.exigirCargo(1L, "cadastrar usuários", Cargo.GERENTE);

        assertSame(marta, operador);
        assertEquals("Marta", MDC.get("usuario"));
        verifyNoInteractions(auditoriaService);
    }

    @Test
    void vendedorPassaQuandoOCargoDeleEstaNaLista() {
        Usuario caio = usuario(2L, "Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));

        Usuario operador = controleAcesso.exigirCargo(2L, "consultar", Cargo.GERENTE, Cargo.VENDEDOR);

        assertSame(caio, operador);
    }

    private Usuario usuario(Long id, String nome, Cargo cargo) {
        Usuario usuario = new Usuario(nome, cargo);
        usuario.setId(id);
        return usuario;
    }
}
