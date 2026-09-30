package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.IntegridadeAuditoriaResponse;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import br.org.edu.ifrn.lojacarro.repository.RegistroAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditoriaServiceTest {

    private BancoEmMemoria banco;
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        banco = new BancoEmMemoria();
        auditoriaService = new AuditoriaService(banco);
    }

    @Test
    void primeiroRegistroDevePartirDoHashInicial() {
        RegistroAuditoria registro = auditoriaService.registrar(AcaoAuditoria.USUARIO_CRIADO, "Marta", "Cadastrou Caio");

        assertEquals(AuditoriaService.HASH_INICIAL, registro.getHashAnterior());
        assertEquals(64, registro.getHash().length());
        assertEquals("USUARIO_CRIADO", registro.getAcao());
    }

    @Test
    void cadaRegistroApontaParaOHashDoAnterior() {
        RegistroAuditoria primeiro = auditoriaService.registrar(AcaoAuditoria.USUARIO_CRIADO, "Marta", "Cadastrou Caio");
        RegistroAuditoria segundo = auditoriaService.registrar(AcaoAuditoria.CARRO_CRIADO, "Marta", "Gol");

        assertEquals(primeiro.getHash(), segundo.getHashAnterior());
        assertNotEquals(primeiro.getHash(), segundo.getHash());
    }

    @Test
    void responsavelEmBrancoViraAnonimoEDetalheNuloViraVazio() {
        RegistroAuditoria registro = auditoriaService.registrar(AcaoAuditoria.ACESSO_NEGADO, "  ", null);

        assertEquals("anonimo", registro.getResponsavel());
        assertEquals("", registro.getDetalhes());
    }

    @Test
    void trilhaSemMexidaDeveSerIntegra() {
        registrarTres();

        IntegridadeAuditoriaResponse resultado = auditoriaService.verificarIntegridade();

        assertTrue(resultado.isIntegra());
        assertEquals(3, resultado.getTotalRegistros());
        assertNull(resultado.getRegistroAdulterado());
    }

    @Test
    void trilhaVaziaTambemEhIntegra() {
        assertTrue(auditoriaService.verificarIntegridade().isIntegra());
    }

    @Test
    void alterarDetalheDeUmRegistroDeveQuebrarACadeia() {
        registrarTres();
        ReflectionTestUtils.setField(banco.registros.get(1), "detalhes", "nada aconteceu aqui");

        IntegridadeAuditoriaResponse resultado = auditoriaService.verificarIntegridade();

        assertFalse(resultado.isIntegra());
        assertEquals(2L, resultado.getRegistroAdulterado());
    }

    @Test
    void apagarUmRegistroDoMeioDeveQuebrarACadeia() {
        registrarTres();
        banco.registros.remove(1);

        IntegridadeAuditoriaResponse resultado = auditoriaService.verificarIntegridade();

        assertFalse(resultado.isIntegra());
        assertEquals(3L, resultado.getRegistroAdulterado());
    }

    @Test
    void hashDependeDeTodosOsCampos() {
        LocalDateTime quando = LocalDateTime.of(2026, 9, 30, 10, 15, 30);

        String original = AuditoriaService.calcularHash("0", quando, "ACAO", "Marta", "detalhe");

        assertEquals(original, AuditoriaService.calcularHash("0", quando, "ACAO", "Marta", "detalhe"));
        assertNotEquals(original, AuditoriaService.calcularHash("0", quando, "ACAO", "Marta", "detalhe!"));
        assertNotEquals(original, AuditoriaService.calcularHash("1", quando, "ACAO", "Marta", "detalhe"));
        assertNotEquals(original, AuditoriaService.calcularHash("0", quando.plusSeconds(1), "ACAO", "Marta", "detalhe"));
    }

    @Test
    void listarRecentesVemDoMaisNovoParaOMaisVelho() {
        registrarTres();

        List<RegistroAuditoria> recentes = auditoriaService.listarRecentes();

        assertEquals(3L, recentes.get(0).getId());
        assertEquals(1L, recentes.get(2).getId());
    }

    private void registrarTres() {
        auditoriaService.registrar(AcaoAuditoria.USUARIO_CRIADO, "Marta", "Cadastrou Caio");
        auditoriaService.registrar(AcaoAuditoria.USUARIO_ATUALIZADO, "Marta", "Caio virou gerente");
        auditoriaService.registrar(AcaoAuditoria.USUARIO_EXCLUIDO, "Marta", "Excluiu Caio");
    }

    private static class BancoEmMemoria implements RegistroAuditoriaRepository {

        private final List<RegistroAuditoria> registros = new ArrayList<>();
        private long proximoId = 1;

        @Override
        public RegistroAuditoria save(RegistroAuditoria registro) {
            ReflectionTestUtils.setField(registro, "id", proximoId++);
            registros.add(registro);
            return registro;
        }

        @Override
        public Optional<RegistroAuditoria> findTopByOrderByIdDesc() {
            return registros.stream().max(Comparator.comparing(RegistroAuditoria::getId));
        }

        @Override
        public List<RegistroAuditoria> findAllByOrderByIdAsc() {
            return registros.stream().sorted(Comparator.comparing(RegistroAuditoria::getId)).toList();
        }

        @Override
        public List<RegistroAuditoria> findTop200ByOrderByIdDesc() {
            return registros.stream().sorted(Comparator.comparing(RegistroAuditoria::getId).reversed()).toList();
        }

        @Override
        public long count() {
            return registros.size();
        }
    }
}
