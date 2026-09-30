package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.IntegridadeAuditoriaResponse;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import br.org.edu.ifrn.lojacarro.security.ControleAcessoUsuario;
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
    private final ControleAcessoUsuario controleAcesso;

    public AuditoriaController(AuditoriaService auditoriaService, ControleAcessoUsuario controleAcesso) {
        this.auditoriaService = auditoriaService;
        this.controleAcesso = controleAcesso;
    }

    @GetMapping
    public List<RegistroAuditoria> listar(
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        controleAcesso.exigirCargo(operadorId, "ver a auditoria", Cargo.GERENTE);
        return auditoriaService.listarRecentes();
    }

    @GetMapping("/integridade")
    public IntegridadeAuditoriaResponse verificarIntegridade(
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        controleAcesso.exigirCargo(operadorId, "verificar a auditoria", Cargo.GERENTE);
        return auditoriaService.verificarIntegridade();
    }
}
