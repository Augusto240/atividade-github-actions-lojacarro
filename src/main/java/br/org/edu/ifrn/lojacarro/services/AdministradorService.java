package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.AdministradorRequest;
import br.org.edu.ifrn.lojacarro.model.Administrador;
import br.org.edu.ifrn.lojacarro.repository.AdministradorRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class AdministradorService {

    private static final Logger log = LoggerFactory.getLogger(AdministradorService.class);

    private final AdministradorRepository administradorRepository;
    private final InputValidator validator;

    public AdministradorService(AdministradorRepository administradorRepository, InputValidator validator) {
        this.administradorRepository = administradorRepository;
        this.validator = validator;
    }

    public List<Administrador> listar() {
        List<Administrador> administradores = administradorRepository.findAll();
        log.info("Listagem de administradores devolveu {} registro(s)", administradores.size());
        return administradores;
    }

    public Administrador buscar(Long id) {
        return administradorRepository.findById(id).orElseThrow(() -> {
            log.warn("Administrador {} nao encontrado", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador " + id + " não encontrado");
        });
    }

    public Administrador criar(AdministradorRequest request) {
        Administrador administrador = new Administrador();
        preencher(administrador, request);
        Administrador salvo = administradorRepository.save(administrador);
        log.info("Cadastrado o {}", salvo);
        return salvo;
    }

    public Administrador atualizar(Long id, AdministradorRequest request) {
        Administrador administrador = buscar(id);
        preencher(administrador, request);
        Administrador salvo = administradorRepository.save(administrador);
        log.info("Atualizado o {}", salvo);
        return salvo;
    }

    public Administrador excluir(Long id) {
        Administrador administrador = buscar(id);
        administradorRepository.delete(administrador);
        log.info("Excluido o {}", administrador);
        return administrador;
    }

    private void preencher(Administrador administrador, AdministradorRequest request) {
        validator.validateNotEmpty(request.nome(), "nome");
        validator.validateFieldLength(request.nome(), "nome");
        validator.validateEmail(request.email());
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        administradorRepository.findByEmail(email)
                .filter(outro -> !outro.getId().equals(administrador.getId()))
                .ifPresent(outro -> {
                    log.warn("Email {} ja esta com o administrador {}", email, outro.getId());
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um administrador com esse e-mail");
                });

        administrador.setNome(validator.sanitize(request.nome()));
        administrador.setEmail(email);
    }
}
