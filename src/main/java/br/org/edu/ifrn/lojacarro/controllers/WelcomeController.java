package br.org.edu.ifrn.lojacarro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/boas-vindas")
    public ResponseEntity<Map<String, String>> boasVindas() {
        return ResponseEntity.ok(Map.of("mensagem", "Bem-vindo a LojaCarro API!"));
    }
}
