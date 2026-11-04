package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.rabbitmq.Enviar;
import br.org.edu.ifrn.lojacarro.security.UsuarioIdentificado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RegistroDeOperacoes {

    private static final Logger log = LoggerFactory.getLogger(RegistroDeOperacoes.class);

    private final AuditoriaService auditoriaService;
    private final Enviar enviar;

    public RegistroDeOperacoes(AuditoriaService auditoriaService, Enviar enviar) {
        this.auditoriaService = auditoriaService;
        this.enviar = enviar;
    }

    public void registrar(AcaoAuditoria acao, UsuarioIdentificado usuario, Long registroId, String descricao) {
        log.debug("[{}] {} por {}: {}", acao.getModulo(), acao, usuario.descricao(), descricao);
        auditoriaService.registrar(acao, usuario.descricao(), descricao);
        enviar.enviarMensagem(new LogOperacaoDTO(acao.getModulo(), acao.name(), LocalDateTime.now().withNano(0),
                usuario.id(), usuario.nome(), registroId, descricao, MDC.get("requestId")));
    }
}
