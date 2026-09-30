package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void qualquerUmPodeConsultarCarros() throws Exception {
        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk());
    }

    @Test
    void cadastrarCarroSemIdentificacaoDeveRetornarUnauthorized() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CarroRequest("Fiat", "Uno", 2015))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vendedorNaoPodeCriarCarro() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .header("X-Usuario-Id", usuario(Cargo.VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CarroRequest("Fiat", "Uno", 2015))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("O cargo VENDEDOR não pode cadastrar carros"));
    }

    @Test
    void vendedorNaoPodeExcluirCarro() throws Exception {
        mockMvc.perform(delete("/carro/1").header("X-Usuario-Id", usuario(Cargo.VENDEDOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void gerentePodeCriarCarro() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .header("X-Usuario-Id", usuario(Cargo.GERENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CarroRequest("Fiat", "Palio", 2017))))
                .andExpect(status().isCreated());
    }

    private Long usuario(Cargo cargo) {
        return usuarioRepository.save(new Usuario("Teste " + cargo, cargo)).getId();
    }
}
