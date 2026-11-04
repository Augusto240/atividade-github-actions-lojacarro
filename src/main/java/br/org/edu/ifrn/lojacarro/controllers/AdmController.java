package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.AdministradorRequest;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Administrador;
import br.org.edu.ifrn.lojacarro.rabbitmq.ConsultaLogs;
import br.org.edu.ifrn.lojacarro.security.IdentificacaoUsuario;
import br.org.edu.ifrn.lojacarro.security.UsuarioIdentificado;
import br.org.edu.ifrn.lojacarro.services.AdministradorService;
import br.org.edu.ifrn.lojacarro.services.RegistroDeOperacoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adm")
public class AdmController {

    private static final Logger log = LoggerFactory.getLogger(AdmController.class);

    private final AdministradorService service;
    private final IdentificacaoUsuario identificacao;
    private final RegistroDeOperacoes operacoes;
    private final ConsultaLogs consultaLogs;

    public AdmController(AdministradorService service, IdentificacaoUsuario identificacao,
            RegistroDeOperacoes operacoes, ConsultaLogs consultaLogs) {
        this.service = service;
        this.identificacao = identificacao;
        this.operacoes = operacoes;
        this.consultaLogs = consultaLogs;
    }

    @GetMapping("/logs")
    public List<String> logs(@RequestParam(defaultValue = "50") int quantidade,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "ver os logs");
        int limite = Math.max(1, Math.min(quantidade, 500));
        List<String> linhas = consultaLogs.ultimasOperacoes(limite);
        log.info("{} consultou {} linha(s) do modulo de log", usuario.descricao(), linhas.size());
        operacoes.registrar(AcaoAuditoria.LOGS_CONSULTADOS, usuario, null, "Consultou as ultimas " + limite + " operacoes");
        return linhas;
    }

    @GetMapping("/administradores")
    public List<Administrador> listar(@RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou administradores", identificacao.identificar(usuarioId));
        return service.listar();
    }

    @GetMapping("/administradores/{id}")
    public Administrador buscar(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou o administrador {}", identificacao.identificar(usuarioId), id);
        return service.buscar(id);
    }

    @PostMapping("/administradores")
    public ResponseEntity<Administrador> criar(@RequestBody AdministradorRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "cadastrar administradores");
        Administrador administrador = service.criar(request);
        log.info("{} cadastrou: {}", usuario.descricao(), administrador);
        operacoes.registrar(AcaoAuditoria.ADMINISTRADOR_CRIADO, usuario, administrador.getId(), administrador.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(administrador);
    }

    @PutMapping("/administradores/{id}")
    public Administrador atualizar(@PathVariable Long id, @RequestBody AdministradorRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "editar administradores");
        Administrador administrador = service.atualizar(id, request);
        log.info("{} atualizou: {}", usuario.descricao(), administrador);
        operacoes.registrar(AcaoAuditoria.ADMINISTRADOR_ATUALIZADO, usuario, administrador.getId(), administrador.toString());
        return administrador;
    }

    @DeleteMapping("/administradores/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "excluir administradores");
        Administrador administrador = service.excluir(id);
        log.info("{} excluiu: {}", usuario.descricao(), administrador);
        operacoes.registrar(AcaoAuditoria.ADMINISTRADOR_EXCLUIDO, usuario, id, administrador.toString());
        return ResponseEntity.noContent().build();
    }
}
