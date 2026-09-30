package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.UsuarioRequest;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    static final int TAMANHO_MAXIMO_NOME = 100;

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private static final String CAMPO_NOME = "nome";

    private final UsuarioRepository usuarioRepository;
    private final InputValidator validator;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, InputValidator validator,
            AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.validator = validator;
        this.auditoriaService = auditoriaService;
    }

    public List<Usuario> listar() {
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByNomeAsc();
        log.info("Listagem de usuarios devolveu {} registro(s)", usuarios.size());
        return usuarios;
    }

    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> {
            log.warn("Usuario {} nao encontrado", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado");
        });
    }

    public Usuario criar(UsuarioRequest request, Usuario operador) {
        String nome = validarNome(request.getNome());
        Cargo cargo = converterCargo(request.getCargo());

        Usuario novo = usuarioRepository.save(new Usuario(nome, cargo));

        log.info("Usuario '{}' cadastrado com id {} e cargo {} por {}", novo.getNome(), novo.getId(), cargo,
                operador.getNome());
        auditoriaService.registrar(AcaoAuditoria.USUARIO_CRIADO, operador.identificacao(), "Cadastrou " + novo);
        return novo;
    }

    public Usuario atualizar(Long id, UsuarioRequest request, Usuario operador) {
        Usuario usuario = buscar(id);
        String nome = validarNome(request.getNome());
        Cargo novoCargo = converterCargo(request.getCargo());

        boolean rebaixandoGerente = usuario.getCargo() == Cargo.GERENTE && novoCargo != Cargo.GERENTE;
        if (rebaixandoGerente && usuarioRepository.countByCargo(Cargo.GERENTE) <= 1) {
            log.warn("{} tentou tirar o cargo do ultimo gerente ({})", operador.getNome(), usuario.getNome());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível tirar o cargo do último gerente");
        }

        String antes = usuario.toString();
        usuario.setNome(nome);
        usuario.setCargo(novoCargo);
        Usuario salvo = usuarioRepository.save(usuario);

        log.info("Usuario {} atualizado por {}: {} -> {}", id, operador.getNome(), antes, salvo);
        auditoriaService.registrar(AcaoAuditoria.USUARIO_ATUALIZADO, operador.identificacao(),
                "Antes: " + antes + " | Depois: " + salvo);
        return salvo;
    }

    public void excluir(Long id, Usuario operador) {
        Usuario usuario = buscar(id);

        if (usuario.getId().equals(operador.getId())) {
            log.warn("{} tentou excluir o proprio usuario", operador.getNome());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você não pode excluir o seu próprio usuário");
        }

        usuarioRepository.delete(usuario);

        log.info("Usuario {} excluido por {}", usuario, operador.getNome());
        auditoriaService.registrar(AcaoAuditoria.USUARIO_EXCLUIDO, operador.identificacao(), "Excluiu " + usuario);
    }

    private String validarNome(String nomeRecebido) {
        validator.validateNotEmpty(nomeRecebido, CAMPO_NOME);
        String nome = validator.sanitize(nomeRecebido);
        validator.validateNotEmpty(nome, CAMPO_NOME);

        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            log.warn("Nome recusado por ter {} caracteres", nome.length());
            throw new InputValidator.ValidationException(
                    "nome must not exceed " + TAMANHO_MAXIMO_NOME + " characters", 422);
        }
        return nome;
    }

    private Cargo converterCargo(String cargo) {
        validator.validateNotEmpty(cargo, "cargo");
        try {
            return Cargo.valueOf(cargo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Cargo invalido recebido: '{}'", cargo);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cargo inválido. Use GERENTE ou VENDEDOR");
        }
    }
}
