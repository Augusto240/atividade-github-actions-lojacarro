package br.org.edu.ifrn.LojaCarro.security;

import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationContextUtilTest {

    private final AuthenticationContextUtil util = new AuthenticationContextUtil();

    @Test
    void getLoggedUserDeveRetornarUsuarioQuandoAtributoPresente() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = new User("gerente@teste.com", "hash", UserRole.GERENTE);
        when(request.getAttribute("user")).thenReturn(user);

        assertSame(user, util.getLoggedUser(request));
    }

    @Test
    void getLoggedUserDeveRetornarNullQuandoAtributoAusente() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("user")).thenReturn(null);

        assertNull(util.getLoggedUser(request));
    }

    @Test
    void getLoggedUserDeveRetornarNullQuandoAtributoDeOutroTipo() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("user")).thenReturn("nao-e-um-user");

        assertNull(util.getLoggedUser(request));
    }
}
