package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.IntegridadeAuditoriaResponse;
import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import br.org.edu.ifrn.lojacarro.security.IdentificacaoUsuario;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final IdentificacaoUsuario identificacao;

    public AuditoriaController(AuditoriaService auditoriaService, IdentificacaoUsuario identificacao) {
        this.auditoriaService = auditoriaService;
        this.identificacao = identificacao;
    }

    @GetMapping
    public List<RegistroAuditoria> listar(
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        identificacao.exigir(usuarioId, "ver a auditoria");
        return auditoriaService.listarRecentes();
    }

    @GetMapping("/integridade")
    public IntegridadeAuditoriaResponse verificarIntegridade(
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        identificacao.exigir(usuarioId, "verificar a auditoria");
        return auditoriaService.verificarIntegridade();
    }
}
