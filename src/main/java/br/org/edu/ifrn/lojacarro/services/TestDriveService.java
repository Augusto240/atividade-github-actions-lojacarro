package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.TestDriveRequest;
import br.org.edu.ifrn.lojacarro.model.TestDrive;
import br.org.edu.ifrn.lojacarro.repository.TestDriveRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TestDriveService {

    private static final Logger log = LoggerFactory.getLogger(TestDriveService.class);

    private final TestDriveRepository testDriveRepository;
    private final CarroService carroService;
    private final InputValidator validator;

    public TestDriveService(TestDriveRepository testDriveRepository, CarroService carroService,
            InputValidator validator) {
        this.testDriveRepository = testDriveRepository;
        this.carroService = carroService;
        this.validator = validator;
    }

    public List<TestDrive> listar() {
        List<TestDrive> testDrives = testDriveRepository.findAll();
        log.info("Listagem de test drives devolveu {} registro(s)", testDrives.size());
        return testDrives;
    }

    public TestDrive buscar(Long id) {
        return testDriveRepository.findById(id).orElseThrow(() -> {
            log.warn("Test drive {} nao encontrado", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Test drive " + id + " não encontrado");
        });
    }

    public TestDrive criar(TestDriveRequest request) {
        TestDrive testDrive = new TestDrive();
        preencher(testDrive, request);
        TestDrive salvo = testDriveRepository.save(testDrive);
        log.info("Agendado o {}", salvo);
        return salvo;
    }

    public TestDrive atualizar(Long id, TestDriveRequest request) {
        TestDrive testDrive = buscar(id);
        preencher(testDrive, request);
        TestDrive salvo = testDriveRepository.save(testDrive);
        log.info("Remarcado o {}", salvo);
        return salvo;
    }

    public TestDrive excluir(Long id) {
        TestDrive testDrive = buscar(id);
        testDriveRepository.delete(testDrive);
        log.info("Cancelado o {}", testDrive);
        return testDrive;
    }

    private void preencher(TestDrive testDrive, TestDriveRequest request) {
        carroService.verificarExiste(request.carroId());
        validator.validateNotEmpty(request.cliente(), "cliente");
        validator.validateFieldLength(request.cliente(), "cliente");
        validator.validateFieldLength(request.observacao(), "observacao");
        if (request.dataHora() == null) {
            log.warn("Test drive recusado: sem data");
            throw new InputValidator.ValidationException("Informe a data e a hora do test drive", 400);
        }

        testDrive.setCarroId(request.carroId());
        testDrive.setCliente(validator.sanitize(request.cliente()));
        testDrive.setDataHora(request.dataHora().withSecond(0).withNano(0));
        testDrive.setObservacao(validator.sanitize(request.observacao()));
    }
}
