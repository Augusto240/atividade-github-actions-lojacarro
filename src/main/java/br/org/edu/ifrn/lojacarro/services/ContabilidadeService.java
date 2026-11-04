package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.LancamentoRequest;
import br.org.edu.ifrn.lojacarro.model.Lancamento;
import br.org.edu.ifrn.lojacarro.model.TipoLancamento;
import br.org.edu.ifrn.lojacarro.repository.LancamentoRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class ContabilidadeService {

    private static final Logger log = LoggerFactory.getLogger(ContabilidadeService.class);

    private final LancamentoRepository lancamentoRepository;
    private final InputValidator validator;

    public ContabilidadeService(LancamentoRepository lancamentoRepository, InputValidator validator) {
        this.lancamentoRepository = lancamentoRepository;
        this.validator = validator;
    }

    public List<Lancamento> listar() {
        List<Lancamento> lancamentos = lancamentoRepository.findAll();
        log.info("Listagem de lancamentos devolveu {} registro(s)", lancamentos.size());
        return lancamentos;
    }

    public Lancamento buscar(Long id) {
        return lancamentoRepository.findById(id).orElseThrow(() -> {
            log.warn("Lancamento {} nao encontrado", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento " + id + " não encontrado");
        });
    }

    public Lancamento criar(LancamentoRequest request) {
        Lancamento lancamento = new Lancamento();
        preencher(lancamento, request);
        Lancamento salvo = lancamentoRepository.save(lancamento);
        log.info("Registrado o {}", salvo);
        return salvo;
    }

    public Lancamento atualizar(Long id, LancamentoRequest request) {
        Lancamento lancamento = buscar(id);
        preencher(lancamento, request);
        Lancamento salvo = lancamentoRepository.save(lancamento);
        log.info("Corrigido o {}", salvo);
        return salvo;
    }

    public Lancamento excluir(Long id) {
        Lancamento lancamento = buscar(id);
        lancamentoRepository.delete(lancamento);
        log.info("Excluido o {}", lancamento);
        return lancamento;
    }

    private void preencher(Lancamento lancamento, LancamentoRequest request) {
        validator.validateNotEmpty(request.descricao(), "descricao");
        validator.validateFieldLength(request.descricao(), "descricao");
        if (request.valor() == null || request.valor().signum() <= 0) {
            log.warn("Lancamento recusado: valor {} invalido", request.valor());
            throw new InputValidator.ValidationException("O valor precisa ser maior que zero", 400);
        }

        lancamento.setDescricao(validator.sanitize(request.descricao()));
        lancamento.setTipo(converterTipo(request.tipo()));
        lancamento.setValor(request.valor());
        lancamento.setData(request.data() == null ? LocalDate.now() : request.data());
    }

    private TipoLancamento converterTipo(String tipo) {
        validator.validateNotEmpty(tipo, "tipo");
        try {
            return TipoLancamento.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de lancamento invalido: '{}'", tipo);
            throw new InputValidator.ValidationException("Tipo inválido. Use RECEITA ou DESPESA", 400);
        }
    }
}
