package br.org.edu.ifrn.lojacarro.config;

import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialUsuarios implements ApplicationRunner {

    static final String NOME_GERENTE_PADRAO = "Administrador";

    private static final Logger log = LoggerFactory.getLogger(CargaInicialUsuarios.class);

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public CargaInicialUsuarios(UsuarioRepository usuarioRepository, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void run(ApplicationArguments args) {
        long total = usuarioRepository.count();
        if (total > 0) {
            log.info("Tabela de usuarios ja tem {} registro(s), nada a criar", total);
            return;
        }

        Usuario gerente = usuarioRepository.save(new Usuario(NOME_GERENTE_PADRAO, Cargo.GERENTE));
        log.warn("Nenhum usuario cadastrado. Criei o gerente padrao '{}' com id {}", gerente.getNome(), gerente.getId());
        auditoriaService.registrar(AcaoAuditoria.USUARIO_CRIADO, "sistema", "Carga inicial: " + gerente);
    }
}
