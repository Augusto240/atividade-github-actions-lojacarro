package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class CarroRepositoryIntegrationTest {

    @Autowired
    private CarroRepository carroRepository;

    @Test
    void salvarDevePersistir() {
        Carro salvo = carroRepository.save(criarCarro("Gol", 2020));

        assertTrue(carroRepository.findById(salvo.getId()).isPresent());
    }

    @Test
    void buscarPorIdInexistenteDeveRetornarVazio() {
        assertTrue(carroRepository.findById(9999L).isEmpty());
    }

    @Test
    void deletarDeveRemoverDoBanco() {
        Carro salvo = carroRepository.save(criarCarro("Onix", 2022));

        carroRepository.deleteById(salvo.getId());

        assertTrue(carroRepository.findById(salvo.getId()).isEmpty());
    }

    @Test
    void listarTodosDeveRetornarLista() {
        carroRepository.deleteAll();
        carroRepository.save(criarCarro("Palio", 2016));
        carroRepository.save(criarCarro("Celta", 2014));

        List<Carro> carros = carroRepository.findAll();

        assertEquals(2, carros.size());
    }

    private Carro criarCarro(String modelo, int ano) {
        Carro carro = new Carro();
        carro.setMarca("Marca");
        carro.setModelo(modelo);
        carro.setAno(ano);
        return carro;
    }
}
