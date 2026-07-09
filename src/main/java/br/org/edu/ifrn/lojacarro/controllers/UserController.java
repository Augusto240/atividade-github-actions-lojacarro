package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.CreateUserRequest;
import br.org.edu.ifrn.lojacarro.dto.LoginResponse;
import br.org.edu.ifrn.lojacarro.model.User;
import br.org.edu.ifrn.lojacarro.services.UserService;
import br.org.edu.ifrn.lojacarro.security.RoleBasedAccessService;
import br.org.edu.ifrn.lojacarro.security.AuthenticationContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;
    private final RoleBasedAccessService roleBasedAccessService;
    private final AuthenticationContextUtil authUtil;

    public UserController(UserService userService, RoleBasedAccessService roleBasedAccessService,
            AuthenticationContextUtil authUtil) {
        this.userService = userService;
        this.roleBasedAccessService = roleBasedAccessService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> criarUsuario(@RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        User loggedUser = authUtil.getLoggedUser(httpRequest);
        roleBasedAccessService.checkGerenteOnly(loggedUser.getRole());

        User user = userService.register(request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse("", user.getEmail(), user.getRole().name()));
    }
}

