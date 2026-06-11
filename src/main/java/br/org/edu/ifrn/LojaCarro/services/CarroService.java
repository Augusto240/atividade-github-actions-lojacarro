package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.CarroRequest;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.security.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private InputValidator validator;

    public Carro save(CarroRequest request) {
        validator.validateNotEmpty(request.getMarca(), "marca");
        validator.validateNotEmpty(request.getModelo(), "modelo");
        validator.validateFieldLength(request.getMarca(), "marca");
        validator.validateFieldLength(request.getModelo(), "modelo");

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
        validator.validateFieldLength(marca, "marca");
        return carroRepository.findByMarcaIgnoreCase(validator.sanitize(marca));
    }

    public Carro update(CarroRequest request, Long id) {
        validator.validateNotEmpty(request.getMarca(), "marca");
        validator.validateNotEmpty(request.getModelo(), "modelo");
        validator.validateFieldLength(request.getMarca(), "marca");
        validator.validateFieldLength(request.getModelo(), "modelo");

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
