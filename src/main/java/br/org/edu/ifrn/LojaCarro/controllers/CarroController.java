package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.CarroRequest;
import br.org.edu.ifrn.LojaCarro.dto.CarroResponse;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.security.RoleBasedAccessService;
import br.org.edu.ifrn.LojaCarro.security.AuthenticationContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/carro", "/veiculos"})
public class CarroController {

    @Autowired
    private CarroService carroService;

    @Autowired
    private RoleBasedAccessService roleBasedAccessService;

    @Autowired
    private AuthenticationContextUtil authUtil;

    @PostMapping({"", "/salvar"})
    public ResponseEntity<CarroResponse> salvarCarro(@RequestBody CarroRequest request, HttpServletRequest httpRequest) {
        User loggedUser = authUtil.getLoggedUser(httpRequest);
        roleBasedAccessService.checkGerenteOnly(loggedUser.getRole());

        Carro carro = carroService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(carro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarroResponse> atualizarCarro(@PathVariable Long id, @RequestBody CarroRequest request, HttpServletRequest httpRequest) {
        User loggedUser = authUtil.getLoggedUser(httpRequest);
        roleBasedAccessService.checkGerenteOnly(loggedUser.getRole());

        Carro carro = carroService.update(request, id);
        return ResponseEntity.ok(mapToResponse(carro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id, HttpServletRequest httpRequest) {
        User loggedUser = authUtil.getLoggedUser(httpRequest);
        roleBasedAccessService.checkGerenteOnly(loggedUser.getRole());

        carroService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroResponse> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = carroService.findById(id);
        return carro.map(c -> ResponseEntity.ok(mapToResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarroResponse>> pesquisarTodosCarros(@RequestParam(required = false) String marca) {
        List<Carro> carros = marca == null ? carroService.findAll() : carroService.findByMarca(marca);
        return ResponseEntity.ok(carros.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    private CarroResponse mapToResponse(Carro carro) {
        return new CarroResponse(carro.getId(), carro.getMarca(), carro.getModelo(), carro.getAno());
    }
}
