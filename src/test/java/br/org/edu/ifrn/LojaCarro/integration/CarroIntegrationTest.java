package br.org.edu.ifrn.LojaCarro.integration;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class CarroIntegrationTest {

    @Autowired
    private CarroService carroService;

    @Autowired
    private CarroRepository carroRepository;

    @Test
    void salvarDevePersistirNoBanco() {
        Carro carro = criarCarro("Gol", 2020);

        Carro salvo = carroService.save(carro);

        assertNotNull(salvo.getId());
        assertTrue(carroRepository.findById(salvo.getId()).isPresent());
    }

    @Test
    void deletarDeveRemoverDoBanco() {
        Carro salvo = carroService.save(criarCarro("Onix", 2022));

        carroService.deleteById(salvo.getId());

        assertTrue(carroRepository.findById(salvo.getId()).isEmpty());
    }

    @Test
    void atualizarDevePersistirAlteracoes() {
        Carro salvo = carroService.save(criarCarro("HB20", 2021));
        salvo.setModelo("HB20S");
        salvo.setAno(2023);

        Carro atualizado = carroService.update(salvo);

        assertEquals("HB20S", atualizado.getModelo());
        assertEquals(2023, atualizado.getAno());
    }

    @Test
    void procurarPorIdDeveRetornarCarro() {
        Carro salvo = carroService.save(criarCarro("Uno", 2015));

        Optional<Carro> encontrado = carroService.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Uno", encontrado.get().getModelo());
    }

    @Test
    void procurarPorIdInexistenteDeveRetornarVazio() {
        Optional<Carro> encontrado = carroService.findById(9999L);

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void listarTodosDeveRetornarCarrosPersistidos() {
        carroRepository.deleteAll();
        carroService.save(criarCarro("Palio", 2016));
        carroService.save(criarCarro("Celta", 2014));

        List<Carro> carros = carroService.findAll();

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
