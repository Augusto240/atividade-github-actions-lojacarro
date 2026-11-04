package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.VendaRequest;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Venda;
import br.org.edu.ifrn.lojacarro.security.IdentificacaoUsuario;
import br.org.edu.ifrn.lojacarro.security.UsuarioIdentificado;
import br.org.edu.ifrn.lojacarro.services.VendaService;
import br.org.edu.ifrn.lojacarro.services.RegistroDeOperacoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private static final Logger log = LoggerFactory.getLogger(VendaController.class);

    private final VendaService service;
    private final IdentificacaoUsuario identificacao;
    private final RegistroDeOperacoes operacoes;

    public VendaController(VendaService service, IdentificacaoUsuario identificacao, RegistroDeOperacoes operacoes) {
        this.service = service;
        this.identificacao = identificacao;
        this.operacoes = operacoes;
    }

    @GetMapping
    public List<Venda> listar(@RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou vendas", identificacao.identificar(usuarioId));
        return service.listar();
    }

    @GetMapping("/{id}")
    public Venda buscar(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        log.info("{} consultou a venda {}", identificacao.identificar(usuarioId), id);
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Venda> criar(@RequestBody VendaRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "cadastrar vendas");
        Venda venda = service.criar(request);
        log.info("{} cadastrou: {}", usuario.descricao(), venda);
        operacoes.registrar(AcaoAuditoria.VENDA_CRIADA, usuario, venda.getId(), venda.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(venda);
    }

    @PutMapping("/{id}")
    public Venda atualizar(@PathVariable Long id, @RequestBody VendaRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "editar vendas");
        Venda venda = service.atualizar(id, request);
        log.info("{} atualizou: {}", usuario.descricao(), venda);
        operacoes.registrar(AcaoAuditoria.VENDA_ATUALIZADA, usuario, venda.getId(), venda.toString());
        return venda;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        UsuarioIdentificado usuario = identificacao.exigir(usuarioId, "excluir vendas");
        Venda venda = service.excluir(id);
        log.info("{} excluiu: {}", usuario.descricao(), venda);
        operacoes.registrar(AcaoAuditoria.VENDA_EXCLUIDA, usuario, id, venda.toString());
        return ResponseEntity.noContent().build();
    }
}
