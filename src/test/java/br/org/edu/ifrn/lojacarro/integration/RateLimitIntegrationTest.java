package br.org.edu.ifrn.lojacarro.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestPropertySource(properties = {
        "ratelimit.max-requests=5",
        "ratelimit.window-seconds=2"
})
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar429AposExcederLimiteDeRequisicoes() throws Exception {
        int totalRequisicoes = 6;
        int ultimoStatus = 0;

        for (int i = 0; i < totalRequisicoes; i++) {
            MvcResult result = mockMvc.perform(get("/carro")).andReturn();
            ultimoStatus = result.getResponse().getStatus();
        }

        assertEquals(429, ultimoStatus, "Esperava 429 na requisicao que excede o limite, mas obteve " + ultimoStatus);
    }
}
