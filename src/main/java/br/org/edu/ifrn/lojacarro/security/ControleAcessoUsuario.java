package br.org.edu.ifrn.lojacarro.security;

import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Component
public class ControleAcessoUsuario {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private static final Logger log = LoggerFactory.getLogger(ControleAcessoUsuario.class);

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public ControleAcessoUsuario(UsuarioRepository usuarioRepository, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public Usuario exigirCargo(Long operadorId, String acao, Cargo... cargosPermitidos) {
        if (operadorId == null) {
            log.warn("Tentativa de {} sem dizer quem esta usando (header {} ausente)", acao, HEADER_USUARIO);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Header " + HEADER_USUARIO + " não informado");
        }

        Usuario operador = usuarioRepository.findById(operadorId).orElseThrow(() -> {
            log.warn("Header {} veio com o usuario {} que nao existe", HEADER_USUARIO, operadorId);
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário " + operadorId + " não existe");
        });

        MDC.put("usuario", operador.getNome());

        if (Arrays.stream(cargosPermitidos).noneMatch(cargo -> cargo == operador.getCargo())) {
            log.warn("Acesso negado: {} tentou {} mas o cargo {} nao permite", operador.getNome(), acao,
                    operador.getCargo());
            auditoriaService.registrar(AcaoAuditoria.ACESSO_NEGADO, operador.identificacao(),
                    "Tentou " + acao + " sem permissão");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O cargo " + operador.getCargo() + " não pode " + acao);
        }

        log.debug("{} ({}) liberado para {}", operador.getNome(), operador.getCargo(), acao);
        return operador;
    }
}
