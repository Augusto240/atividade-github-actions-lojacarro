package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.cliente.UsuarioClient;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class IdentificacaoUsuario {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private static final Logger log = LoggerFactory.getLogger(IdentificacaoUsuario.class);

    private final UsuarioClient usuarioClient;
    private final AuditoriaService auditoriaService;

    public IdentificacaoUsuario(UsuarioClient usuarioClient, AuditoriaService auditoriaService) {
        this.usuarioClient = usuarioClient;
        this.auditoriaService = auditoriaService;
    }

    public UsuarioIdentificado exigir(Long usuarioId, String acao) {
        if (usuarioId == null) {
            log.warn("Tentativa de {} sem o header {}", acao, HEADER_USUARIO);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Header " + HEADER_USUARIO + " não informado");
        }

        String nome = usuarioClient.buscarNome(usuarioId).orElseThrow(() -> {
            log.warn("Usuario {} nao existe na API de usuarios, bloqueando {}", usuarioId, acao);
            auditoriaService.registrar(AcaoAuditoria.ACESSO_NEGADO, "#" + usuarioId,
                    "Usuário inexistente tentou " + acao);
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário " + usuarioId + " não existe");
        });

        MDC.put("usuario", nome);
        return new UsuarioIdentificado(usuarioId, nome);
    }

    public String identificar(Long usuarioId) {
        if (usuarioId == null) {
            return "anonimo";
        }
        try {
            return usuarioClient.buscarNome(usuarioId)
                    .map(nome -> {
                        MDC.put("usuario", nome);
                        return new UsuarioIdentificado(usuarioId, nome).descricao();
                    })
                    .orElse("#" + usuarioId + " (inexistente)");
        } catch (ResponseStatusException e) {
            log.warn("Seguindo sem o nome do usuario {} porque a API de usuarios nao respondeu", usuarioId);
            return "#" + usuarioId;
        }
    }
}
