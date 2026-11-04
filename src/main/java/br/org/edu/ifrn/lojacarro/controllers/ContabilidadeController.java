package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.LancamentoRequest;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Lancamento;
import br.org.edu.ifrn.lojacarro.security.IdentificacaoUsuario;
import br.org.edu.ifrn.lojacarro.security.UsuarioIdentificado;
import br.org.edu.ifrn.lojacarro.services.ContabilidadeService;
import br.org.edu.ifrn.lojacarro.services.RegistroDeOperacoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contabilidade")
public class ContabilidadeController {

    private static final Logger log = LoggerFactory.getLogger(ContabilidadeController.class);

    private final ContabilidadeService service;
    private final IdentificacaoUsuario identificacao;
    private final RegistroDeOperacoes operacoes;

    public ContabilidadeController(ContabilidadeService service, IdentificacaoUsuario identificacao, RegistroDeOperacoes operacoes) {
        this.service = service;
        this.identificacao = identificacao;
        this.operacoes = operacoes;
    }

    @GetMapping
    public List<Lancamento> listar(@RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou lancamentos", identificacao.identificar(usuarioId));
        return service.listar();
    }

    @GetMapping("/{id}")
    public Lancamento buscar(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou o lancamento {}", identificacao.identificar(usuarioId), id);
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Lancamento> criar(@RequestBody LancamentoRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "cadastrar lancamentos");
        Lancamento lancamento = service.criar(request);
        log.info("{} cadastrou: {}", usuario.descricao(), lancamento);
        operacoes.registrar(AcaoAuditoria.LANCAMENTO_CRIADO, usuario, lancamento.getId(), lancamento.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamento);
    }

    @PutMapping("/{id}")
    public Lancamento atualizar(@PathVariable Long id, @RequestBody LancamentoRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "editar lancamentos");
        Lancamento lancamento = service.atualizar(id, request);
        log.info("{} atualizou: {}", usuario.descricao(), lancamento);
        operacoes.registrar(AcaoAuditoria.LANCAMENTO_ATUALIZADO, usuario, lancamento.getId(), lancamento.toString());
        return lancamento;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "excluir lancamentos");
        Lancamento lancamento = service.excluir(id);
        log.info("{} excluiu: {}", usuario.descricao(), lancamento);
        operacoes.registrar(AcaoAuditoria.LANCAMENTO_EXCLUIDO, usuario, id, lancamento.toString());
        return ResponseEntity.noContent().build();
    }
}
