package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.CreateUserRequest;
import br.org.edu.ifrn.LojaCarro.dto.LoginResponse;
import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.services.UserService;
import br.org.edu.ifrn.LojaCarro.security.RoleBasedAccessService;
import br.org.edu.ifrn.LojaCarro.security.AuthenticationContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleBasedAccessService roleBasedAccessService;

    @Autowired
    private AuthenticationContextUtil authUtil;

    @PostMapping
    public ResponseEntity<LoginResponse> criarUsuario(@RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        User loggedUser = authUtil.getLoggedUser(httpRequest);
        roleBasedAccessService.checkGerenteOnly(loggedUser.getRole());

        User user = userService.register(request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse("", user.getEmail(), user.getRole().name()));
    }
}

