package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.VendaRequest;
import br.org.edu.ifrn.lojacarro.model.Venda;
import br.org.edu.ifrn.lojacarro.repository.VendaRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class VendaService {

    private static final Logger log = LoggerFactory.getLogger(VendaService.class);

    private final VendaRepository vendaRepository;
    private final CarroService carroService;
    private final InputValidator validator;

    public VendaService(VendaRepository vendaRepository, CarroService carroService, InputValidator validator) {
        this.vendaRepository = vendaRepository;
        this.carroService = carroService;
        this.validator = validator;
    }

    public List<Venda> listar() {
        List<Venda> vendas = vendaRepository.findAll();
        log.info("Listagem de vendas devolveu {} registro(s)", vendas.size());
        return vendas;
    }

    public Venda buscar(Long id) {
        return vendaRepository.findById(id).orElseThrow(() -> {
            log.warn("Venda {} nao encontrada", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda " + id + " não encontrada");
        });
    }

    public Venda criar(VendaRequest request) {
        Venda venda = new Venda();
        preencher(venda, request);
        Venda salva = vendaRepository.save(venda);
        log.info("Salva a {}", salva);
        return salva;
    }

    public Venda atualizar(Long id, VendaRequest request) {
        Venda venda = buscar(id);
        preencher(venda, request);
        Venda salva = vendaRepository.save(venda);
        log.info("Atualizada a {}", salva);
        return salva;
    }

    public Venda excluir(Long id) {
        Venda venda = buscar(id);
        vendaRepository.delete(venda);
        log.info("Excluida a {}", venda);
        return venda;
    }

    private void preencher(Venda venda, VendaRequest request) {
        carroService.verificarExiste(request.carroId());
        validator.validateNotEmpty(request.cliente(), "cliente");
        validator.validateFieldLength(request.cliente(), "cliente");
        if (request.valor() == null || request.valor().signum() <= 0) {
            log.warn("Venda recusada: valor {} invalido", request.valor());
            throw new InputValidator.ValidationException("O valor da venda precisa ser maior que zero", 400);
        }

        venda.setCarroId(request.carroId());
        venda.setCliente(validator.sanitize(request.cliente()));
        venda.setValor(request.valor());
        venda.setDataVenda(request.dataVenda() == null ? LocalDate.now() : request.dataVenda());
    }
}
