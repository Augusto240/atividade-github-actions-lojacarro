package br.org.edu.ifrn.LojaCarro.security;

import br.org.edu.ifrn.LojaCarro.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleBasedAccessServiceTest {

    private final RoleBasedAccessService service = new RoleBasedAccessService();

    @Test
    void checkGerenteOnlyDevePassarParaGerente() {
        assertDoesNotThrow(() -> service.checkGerenteOnly(UserRole.GERENTE));
    }

    @Test
    void checkGerenteOnlyDeveLancarForbiddenParaVendedor() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.checkGerenteOnly(UserRole.VENDEDOR));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void checkRoleDevePassarQuandoRolesIguais() {
        assertDoesNotThrow(() -> service.checkRole(UserRole.VENDEDOR, UserRole.VENDEDOR));
    }

    @Test
    void checkRoleDevePassarQuandoUsuarioForGerenteIndependenteDaRoleExigida() {
        assertDoesNotThrow(() -> service.checkRole(UserRole.GERENTE, UserRole.VENDEDOR));
    }

    @Test
    void checkRoleDeveLancarForbiddenQuandoRolesDiferentesENaoForGerente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.checkRole(UserRole.VENDEDOR, UserRole.GERENTE));
        assertEquals(403, ex.getStatusCode().value());
    }
}
