package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.dto.CarroResponse;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.security.IdentificacaoUsuario;
import br.org.edu.ifrn.lojacarro.services.AuditoriaService;
import br.org.edu.ifrn.lojacarro.services.CarroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/carro", "/veiculos"})
public class CarroController {

    private static final Logger log = LoggerFactory.getLogger(CarroController.class);

    private final CarroService carroService;
    private final IdentificacaoUsuario identificacao;
    private final AuditoriaService auditoriaService;

    public CarroController(CarroService carroService, IdentificacaoUsuario identificacao,
            AuditoriaService auditoriaService) {
        this.carroService = carroService;
        this.identificacao = identificacao;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping({"", "/salvar"})
    public ResponseEntity<CarroResponse> salvarCarro(@RequestBody CarroRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        String quem = identificacao.exigir(usuarioId, "cadastrar carros");

        Carro carro = carroService.save(request);
        log.info("{} cadastrou o {}", quem, descrever(carro));
        auditoriaService.registrar(AcaoAuditoria.CARRO_CRIADO, quem, "Cadastrou " + descrever(carro));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(carro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarroResponse> atualizarCarro(@PathVariable Long id, @RequestBody CarroRequest request,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        String quem = identificacao.exigir(usuarioId, "editar carros");

        Carro carro = carroService.update(request, id);
        log.info("{} atualizou o {}", quem, descrever(carro));
        auditoriaService.registrar(AcaoAuditoria.CARRO_ATUALIZADO, quem, "Atualizou " + descrever(carro));
        return ResponseEntity.ok(mapToResponse(carro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        String quem = identificacao.exigir(usuarioId, "excluir carros");

        carroService.deleteById(id);
        log.info("{} excluiu o carro {}", quem, id);
        auditoriaService.registrar(AcaoAuditoria.CARRO_EXCLUIDO, quem, "Excluiu o carro " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroResponse> pesquisarCarroPorId(@PathVariable Long id,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        String quem = identificacao.identificar(usuarioId);
        Optional<Carro> carro = carroService.findById(id);
        if (carro.isEmpty()) {
            log.warn("{} procurou o carro {} que nao existe", quem, id);
        } else {
            log.info("{} consultou o carro {}", quem, id);
        }
        return carro.map(c -> ResponseEntity.ok(mapToResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarroResponse>> pesquisarTodosCarros(@RequestParam(required = false) String marca,
            @RequestHeader(value = IdentificacaoUsuario.HEADER_USUARIO, required = false) Long usuarioId) {
        String quem = identificacao.identificar(usuarioId);
        List<Carro> carros = marca == null ? carroService.findAll() : carroService.findByMarca(marca);
        log.info("{} consultou os carros (marca={}) e viu {} resultado(s)", quem, marca, carros.size());
        return ResponseEntity.ok(carros.stream()
                .map(this::mapToResponse)
                .toList());
    }

    private CarroResponse mapToResponse(Carro carro) {
        return new CarroResponse(carro.getId(), carro.getMarca(), carro.getModelo(), carro.getAno());
    }

    private String descrever(Carro carro) {
        return "carro #" + carro.getId() + " " + carro.getMarca() + " " + carro.getModelo() + " " + carro.getAno();
    }
}
