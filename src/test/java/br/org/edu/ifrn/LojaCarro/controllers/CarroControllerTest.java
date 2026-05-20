package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional

class CarroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarroRepository carroRepository;

    @Test
    void salvarDevePersistirNoBanco() throws Exception {
        Carro requisicao = criarCarro("Gol", 2020);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.modelo").value("Gol"))
                .andExpect(jsonPath("$.ano").value(2020));

        List<Carro> carros = carroRepository.findAll();
        assertEquals(3, carros.size());
        assertEquals("Gol", carros.get(2).getModelo());
    }

    @Test
    void atualizarDeveAlterarDadosNoBanco() throws Exception {
        Carro salvo = carroRepository.save(criarCarro("Onix", 2022));
        Carro atualizacao = criarCarro("Onix Plus", 2023);

        mockMvc.perform(put("/carro/{id}", salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.modelo").value("Onix Plus"))
                .andExpect(jsonPath("$.ano").value(2023));

        Carro atualizado = carroRepository.findById(salvo.getId()).orElseThrow();
        assertEquals("Onix Plus", atualizado.getModelo());
        assertEquals(2023, atualizado.getAno());
    }

    @Test
    void deletarDeveRemoverDoBanco() throws Exception {
        Carro salvo = carroRepository.save(criarCarro("HB20", 2021));

        mockMvc.perform(delete("/carro/{id}", salvo.getId()))
                .andExpect(status().isNoContent());

        assertTrue(carroRepository.findById(salvo.getId()).isEmpty());
    }

    @Test
    void procurarPorIdDeveRetornarOkQuandoEncontrado() throws Exception {
        Carro salvo = carroRepository.save(criarCarro("Uno", 2015));

        mockMvc.perform(get("/carro/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.modelo").value("Uno"))
                .andExpect(jsonPath("$.ano").value(2015));
    }

    @Test
    void procurarPorIdDeveRetornarNotFoundQuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/carro/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarTodosDeveRetornarLista() throws Exception {
        carroRepository.deleteAll();
        carroRepository.save(criarCarro("Palio", 2016));
        carroRepository.save(criarCarro("Celta", 2014));

        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
    }

    private Carro criarCarro(String modelo, int ano) {
        Carro carro = new Carro();
        carro.setModelo(modelo);
        carro.setAno(ano);
        return carro;
    }
}
