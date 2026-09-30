package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class XssSanitizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarroRepository carroRepository;

    @Test
    void marcaComScriptDeveSerSanitizadaNaRespostaENoBanco() throws Exception {
        CarroRequest request = new CarroRequest("<script>alert(1)</script>Toyota", "Corolla", 2022);

        MvcResult result = mockMvc.perform(post("/carro/salvar")
                        .header("X-Usuario-Id", gerenteId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.toLowerCase().contains("<script"));
        assertTrue(body.contains("Toyota"));

        Carro persistido = carroRepository.findAll().stream()
                .filter(c -> c.getModelo().equals("Corolla"))
                .findFirst()
                .orElseThrow();
        assertFalse(persistido.getMarca().toLowerCase().contains("<script"));
        assertTrue(persistido.getMarca().contains("Toyota"));
    }

    private Long gerenteId() {
        return usuarioRepository.save(new Usuario("Gerente Xss", Cargo.GERENTE)).getId();
    }
}
