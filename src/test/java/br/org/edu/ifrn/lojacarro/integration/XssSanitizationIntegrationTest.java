package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.cliente.UsuarioClient;
import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
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

    @MockitoBean
    private UsuarioClient usuarioClient;

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
        when(usuarioClient.buscarNome(1L)).thenReturn(Optional.of("Gerente Xss"));
        return 1L;
    }
}
