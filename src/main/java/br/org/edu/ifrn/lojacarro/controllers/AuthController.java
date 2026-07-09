package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.LoginRequest;
import br.org.edu.ifrn.lojacarro.dto.LoginResponse;
import br.org.edu.ifrn.lojacarro.dto.RegisterRequest;
import br.org.edu.ifrn.lojacarro.model.User;
import br.org.edu.ifrn.lojacarro.security.JwtTokenProvider;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import br.org.edu.ifrn.lojacarro.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final InputValidator validator;

    public AuthController(UserService userService, JwtTokenProvider tokenProvider, InputValidator validator) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.validator = validator;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        validator.validateEmail(request.getEmail());
        validator.validateNotEmpty(request.getPassword(), "password");
        validator.validateNotEmpty(request.getRole(), "role");

        User user = userService.register(request.getEmail(), request.getPassword(), request.getRole());
        String token = tokenProvider.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(token, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        validator.validateEmail(request.getEmail());
        validator.validateNotEmpty(request.getPassword(), "password");

        Optional<User> user = userService.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!userService.verifyPassword(request.getPassword(), user.get().getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.get());
        return ResponseEntity.ok(new LoginResponse(token, user.get().getEmail(), user.get().getRole().name()));
    }
}
