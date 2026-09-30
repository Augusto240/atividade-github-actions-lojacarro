package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.dto.CarroResponse;
import br.org.edu.ifrn.lojacarro.model.AcaoAuditoria;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.security.ControleAcessoUsuario;
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
    private final ControleAcessoUsuario controleAcesso;
    private final AuditoriaService auditoriaService;

    public CarroController(CarroService carroService, ControleAcessoUsuario controleAcesso,
            AuditoriaService auditoriaService) {
        this.carroService = carroService;
        this.controleAcesso = controleAcesso;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping({"", "/salvar"})
    public ResponseEntity<CarroResponse> salvarCarro(@RequestBody CarroRequest request,
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para cadastrar o carro {} {} {}", request.getMarca(), request.getModelo(), request.getAno());
        Usuario operador = controleAcesso.exigirCargo(operadorId, "cadastrar carros", Cargo.GERENTE);

        Carro carro = carroService.save(request);
        auditoriaService.registrar(AcaoAuditoria.CARRO_CRIADO, operador.identificacao(), "Cadastrou " + descrever(carro));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(carro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarroResponse> atualizarCarro(@PathVariable Long id, @RequestBody CarroRequest request,
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para atualizar o carro {}", id);
        Usuario operador = controleAcesso.exigirCargo(operadorId, "editar carros", Cargo.GERENTE);

        Carro carro = carroService.update(request, id);
        auditoriaService.registrar(AcaoAuditoria.CARRO_ATUALIZADO, operador.identificacao(), "Atualizou " + descrever(carro));
        return ResponseEntity.ok(mapToResponse(carro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id,
            @RequestHeader(value = ControleAcessoUsuario.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para excluir o carro {}", id);
        Usuario operador = controleAcesso.exigirCargo(operadorId, "excluir carros", Cargo.GERENTE);

        carroService.deleteById(id);
        auditoriaService.registrar(AcaoAuditoria.CARRO_EXCLUIDO, operador.identificacao(), "Excluiu o carro " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroResponse> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = carroService.findById(id);
        if (carro.isEmpty()) {
            log.warn("Carro {} nao encontrado", id);
        }
        return carro.map(c -> ResponseEntity.ok(mapToResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarroResponse>> pesquisarTodosCarros(@RequestParam(required = false) String marca) {
        List<Carro> carros = marca == null ? carroService.findAll() : carroService.findByMarca(marca);
        log.info("Consulta de carros (marca={}) devolveu {} resultado(s)", marca, carros.size());
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
