package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
import br.org.edu.ifrn.lojacarro.security.InputValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    private static final String CAMPO_MARCA = "marca";
    private static final String CAMPO_MODELO = "modelo";

    private final CarroRepository carroRepository;
    private final InputValidator validator;

    public CarroService(CarroRepository carroRepository, InputValidator validator) {
        this.carroRepository = carroRepository;
        this.validator = validator;
    }

    public Carro save(CarroRequest request) {
        validator.validateNotEmpty(request.getMarca(), CAMPO_MARCA);
        validator.validateNotEmpty(request.getModelo(), CAMPO_MODELO);
        validator.validateFieldLength(request.getMarca(), CAMPO_MARCA);
        validator.validateFieldLength(request.getModelo(), CAMPO_MODELO);

        Carro carro = new Carro();
        carro.setMarca(validator.sanitize(request.getMarca()));
        carro.setModelo(validator.sanitize(request.getModelo()));
        carro.setAno(request.getAno());

        return carroRepository.save(carro);
    }

    public Carro save(Carro carro) {
        return save(new CarroRequest(carro.getMarca(), carro.getModelo(), carro.getAno()));
    }

    public void deleteById(Long id) {
        carroRepository.deleteById(id);
    }

    public Optional<Carro> findById(Long id) {
        return carroRepository.findById(id);
    }

    public List<Carro> findAll() {
        return carroRepository.findAll();
    }

    public List<Carro> findByMarca(String marca) {
        validator.validateFieldLength(marca, CAMPO_MARCA);
        return carroRepository.findByMarcaIgnoreCase(validator.sanitize(marca));
    }

    public Carro update(CarroRequest request, Long id) {
        validator.validateNotEmpty(request.getMarca(), CAMPO_MARCA);
        validator.validateNotEmpty(request.getModelo(), CAMPO_MODELO);
        validator.validateFieldLength(request.getMarca(), CAMPO_MARCA);
        validator.validateFieldLength(request.getModelo(), CAMPO_MODELO);

        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carro not found"));

        carro.setMarca(validator.sanitize(request.getMarca()));
        carro.setModelo(validator.sanitize(request.getModelo()));
        carro.setAno(request.getAno());

        return carroRepository.save(carro);
    }

    public Carro update(Carro carro) {
        if (carro.getId() == null) {
            throw new IllegalArgumentException("Carro id is required");
        }
        return update(new CarroRequest(carro.getMarca(), carro.getModelo(), carro.getAno()), carro.getId());
    }
}
