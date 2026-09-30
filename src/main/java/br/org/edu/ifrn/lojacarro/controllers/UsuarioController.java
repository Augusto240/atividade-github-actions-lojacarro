package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.UsuarioRequest;
import br.org.edu.ifrn.lojacarro.dto.UsuarioResponse;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.security.ControleAcessoUsuario;
import br.org.edu.ifrn.lojacarro.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final ControleAcessoUsuario controleAcesso;

    public UsuarioController(UsuarioService usuarioService, ControleAcessoUsuario controleAcesso) {
        this.usuarioService = usuarioService;
        this.controleAcesso = controleAcesso;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        log.debug("Pedido de listagem de usuarios");
        return usuarioService.listar().stream()
                .map(UsuarioResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        log.debug("Pedido do usuario {}", id);
        return new UsuarioResponse(usuarioService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody UsuarioRequest request,
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para cadastrar usuario '{}' com cargo {}", request.getNome(), request.getCargo());
        Usuario operador = controleAcesso.exigirCargo(operadorId, "cadastrar usuários", Cargo.GERENTE);

        Usuario criado = usuarioService.criar(request, operador);
        return ResponseEntity.created(URI.create("/usuarios/" + criado.getId()))
                .body(new UsuarioResponse(criado));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @RequestBody UsuarioRequest request,
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para atualizar usuario {} para nome='{}' cargo={}", id, request.getNome(), request.getCargo());
        Usuario operador = controleAcesso.exigirCargo(operadorId, "editar usuários", Cargo.GERENTE);

        return new UsuarioResponse(usuarioService.atualizar(id, request, operador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para excluir usuario {}", id);
        Usuario operador = controleAcesso.exigirCargo(operadorId, "excluir usuários", Cargo.GERENTE);

        usuarioService.excluir(id, operador);
        return ResponseEntity.noContent().build();
    }
}
