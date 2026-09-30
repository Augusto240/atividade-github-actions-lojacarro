package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.UsuarioRequest;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private UsuarioService usuarioService;
    private Usuario gerente;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, new InputValidator(), auditoriaService);
        gerente = usuario(1L, "Marta", Cargo.GERENTE);
    }

    @Test
    void criarDeveSalvarUsuarioERegistrarNaAuditoria() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        Usuario criado = usuarioService.criar(new UsuarioRequest("  Joao Pedro  ", "vendedor"), gerente);

        assertEquals(10L, criado.getId());
        assertEquals("Joao Pedro", criado.getNome());
        assertEquals(Cargo.VENDEDOR, criado.getCargo());
        verify(auditoriaService).registrar(eq(AcaoAuditoria.USUARIO_CRIADO), eq("Marta #1 (GERENTE)"),
                contains("Joao Pedro"));
    }

    @Test
    void criarDeveTirarHtmlDoNome() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario criado = usuarioService.criar(new UsuarioRequest("<b>Ana</b>", "GERENTE"), gerente);

        assertEquals("Ana", criado.getNome());
    }

    @Test
    void criarSemNomeDeveDarBadRequest() {
        UsuarioRequest request = new UsuarioRequest("   ", "GERENTE");

        InputValidator.ValidationException ex = assertThrows(InputValidator.ValidationException.class,
                () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode());
        verifyNoInteractions(usuarioRepository, auditoriaService);
    }

    @Test
    void criarComNomeQueSoTemScriptDeveDarBadRequest() {
        UsuarioRequest request = new UsuarioRequest("<script></script>", "GERENTE");

        InputValidator.ValidationException ex = assertThrows(InputValidator.ValidationException.class,
                () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void criarComNomeGrandeDemaisDeveDar422() {
        UsuarioRequest request = new UsuarioRequest("a".repeat(UsuarioService.TAMANHO_MAXIMO_NOME + 1), "GERENTE");

        InputValidator.ValidationException ex = assertThrows(InputValidator.ValidationException.class,
                () -> usuarioService.criar(request, gerente));

        assertEquals(422, ex.getStatusCode());
    }

    @Test
    void criarComCargoQueNaoExisteDeveDarBadRequest() {
        UsuarioRequest request = new UsuarioRequest("Carlos", "DIRETOR");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode().value());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criarSemCargoDeveDarBadRequest() {
        UsuarioRequest request = new UsuarioRequest("Carlos", null);

        InputValidator.ValidationException ex = assertThrows(InputValidator.ValidationException.class,
                () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void listarDeveDevolverOQueVeioDoBanco() {
        List<Usuario> usuarios = List.of(gerente, usuario(2L, "Caio", Cargo.VENDEDOR));
        when(usuarioRepository.findAllByOrderByNomeAsc()).thenReturn(usuarios);

        assertSame(usuarios, usuarioService.listar());
    }

    @Test
    void buscarUsuarioQueNaoExisteDeveDar404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.buscar(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizarDeveTrocarNomeECargoERegistrarAntesEDepois() {
        Usuario caio = usuario(2L, "Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));
        when(usuarioRepository.save(caio)).thenReturn(caio);

        Usuario atualizado = usuarioService.atualizar(2L, new UsuarioRequest("Caio Lima", "GERENTE"), gerente);

        assertEquals("Caio Lima", atualizado.getNome());
        assertEquals(Cargo.GERENTE, atualizado.getCargo());
        verify(auditoriaService).registrar(eq(AcaoAuditoria.USUARIO_ATUALIZADO), eq("Marta #1 (GERENTE)"),
                contains("nome='Caio', cargo=VENDEDOR"));
    }

    @Test
    void naoPodeTirarOCargoDoUltimoGerente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(gerente));
        when(usuarioRepository.countByCargo(Cargo.GERENTE)).thenReturn(1L);
        UsuarioRequest request = new UsuarioRequest("Marta", "VENDEDOR");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.atualizar(1L, request, gerente));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals(Cargo.GERENTE, gerente.getCargo());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void podeRebaixarGerenteQuandoTemOutroGerente() {
        Usuario bruno = usuario(3L, "Bruno", Cargo.GERENTE);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(bruno));
        when(usuarioRepository.countByCargo(Cargo.GERENTE)).thenReturn(2L);
        when(usuarioRepository.save(bruno)).thenReturn(bruno);

        Usuario atualizado = usuarioService.atualizar(3L, new UsuarioRequest("Bruno", "VENDEDOR"), gerente);

        assertEquals(Cargo.VENDEDOR, atualizado.getCargo());
    }

    @Test
    void excluirDeveApagarERegistrarNaAuditoria() {
        Usuario caio = usuario(2L, "Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));

        usuarioService.excluir(2L, gerente);

        verify(usuarioRepository).delete(caio);
        verify(auditoriaService).registrar(eq(AcaoAuditoria.USUARIO_EXCLUIDO), eq("Marta #1 (GERENTE)"),
                contains("Caio"));
    }

    @Test
    void naoPodeExcluirOProprioUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(gerente));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.excluir(1L, gerente));

        assertEquals(409, ex.getStatusCode().value());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void excluirUsuarioQueNaoExisteDeveDar404() {
        when(usuarioRepository.findById(50L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.excluir(50L, gerente));

        assertEquals(404, ex.getStatusCode().value());
        verifyNoInteractions(auditoriaService);
    }

    private Usuario usuario(Long id, String nome, Cargo cargo) {
        Usuario usuario = new Usuario(nome, cargo);
        usuario.setId(id);
        return usuario;
    }
}
