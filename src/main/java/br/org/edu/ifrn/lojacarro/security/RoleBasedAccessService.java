package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.model.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RoleBasedAccessService {

    public void checkRole(UserRole userRole, UserRole requiredRole) {
        if (userRole != requiredRole && userRole != UserRole.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }
    }

    public void checkGerenteOnly(UserRole userRole) {
        if (userRole != UserRole.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only GERENTE can perform this action");
        }
    }
}
