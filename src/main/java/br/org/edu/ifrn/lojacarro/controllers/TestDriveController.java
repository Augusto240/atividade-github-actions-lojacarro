package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.TestDriveRequest;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.TestDrive;
import br.org.edu.ifrn.lojacarro.security.IdentificacaoUsuario;
import br.org.edu.ifrn.lojacarro.security.UsuarioIdentificado;
import br.org.edu.ifrn.lojacarro.services.TestDriveService;
import br.org.edu.ifrn.lojacarro.services.RegistroDeOperacoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-drives")
public class TestDriveController {

    private static final Logger log = LoggerFactory.getLogger(TestDriveController.class);

    private final TestDriveService service;
    private final IdentificacaoUsuario identificacao;
    private final RegistroDeOperacoes operacoes;

    public TestDriveController(TestDriveService service, IdentificacaoUsuario identificacao, RegistroDeOperacoes operacoes) {
        this.service = service;
        this.identificacao = identificacao;
        this.operacoes = operacoes;
    }

    @GetMapping
    public List<TestDrive> listar(@RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou test drives", identificacao.identificar(usuarioId));
        return service.listar();
    }

    @GetMapping("/{id}")
    public TestDrive buscar(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou o test drive {}", identificacao.identificar(usuarioId), id);
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<TestDrive> criar(@RequestBody TestDriveRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "cadastrar test drives");
        TestDrive testDrive = service.criar(request);
        log.info("{} cadastrou: {}", usuario.descricao(), testDrive);
        operacoes.registrar(AcaoAuditoria.TEST_DRIVE_CRIADO, usuario, testDrive.getId(), testDrive.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(testDrive);
    }

    @PutMapping("/{id}")
    public TestDrive atualizar(@PathVariable Long id, @RequestBody TestDriveRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "editar test drives");
        TestDrive testDrive = service.atualizar(id, request);
        log.info("{} atualizou: {}", usuario.descricao(), testDrive);
        operacoes.registrar(AcaoAuditoria.TEST_DRIVE_ATUALIZADO, usuario, testDrive.getId(), testDrive.toString());
        return testDrive;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "excluir test drives");
        TestDrive testDrive = service.excluir(id);
        log.info("{} excluiu: {}", usuario.descricao(), testDrive);
        operacoes.registrar(AcaoAuditoria.TEST_DRIVE_EXCLUIDO, usuario, id, testDrive.toString());
        return ResponseEntity.noContent().build();
    }
}
