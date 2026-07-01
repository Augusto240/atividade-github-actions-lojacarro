package br.org.edu.ifrn.LojaCarro.security;

import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private User user;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("segredo-de-teste-para-assinatura-jwt");
        user = new User("gerente@teste.com", "hash", UserRole.GERENTE);
        user.setId(42L);
    }

    @Test
    void generateTokenERoundtripDeveRetornarDadosCorretos() {
        String token = tokenProvider.generateToken(user);

        assertEquals("gerente@teste.com", tokenProvider.getEmailFromToken(token));
        assertEquals(UserRole.GERENTE, tokenProvider.getRoleFromToken(token));
        assertEquals(42L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void validateTokenDeveRetornarTrueParaTokenValido() {
        String token = tokenProvider.generateToken(user);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateTokenDeveRetornarFalseParaTokenMalformado() {
        assertFalse(tokenProvider.validateToken("token-invalido-nao-jwt"));
    }

    @Test
    void validateTokenDeveRetornarFalseParaTokenAssinadoComOutroSegredo() {
        JwtTokenProvider outroProvider = new JwtTokenProvider("outro-segredo-completamente-diferente");
        String token = outroProvider.generateToken(user);

        assertFalse(tokenProvider.validateToken(token));
    }

    @Test
    void extractTokenDeveRemoverPrefixoBearer() {
        assertEquals("abc123", tokenProvider.extractToken("Bearer abc123"));
    }

    @Test
    void extractTokenDeveRetornarValorOriginalQuandoSemPrefixo() {
        assertEquals("abc123", tokenProvider.extractToken("abc123"));
    }

    @Test
    void extractTokenDeveRetornarNullQuandoEntradaNull() {
        assertNull(tokenProvider.extractToken(null));
    }

    @Test
    void construtorDeveLancarExcecaoQuandoSecretForNuloOuVazio() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(null));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("  "));
    }
}
