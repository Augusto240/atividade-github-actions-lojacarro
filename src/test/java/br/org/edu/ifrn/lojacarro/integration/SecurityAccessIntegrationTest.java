package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.model.User;
import br.org.edu.ifrn.lojacarro.model.UserRole;
import br.org.edu.ifrn.lojacarro.repository.UserRepository;
import br.org.edu.ifrn.lojacarro.security.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void requisicaoSemHeaderAuthorizationDeveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/carro"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requisicaoComTokenInvalidoDeveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/carro")
                        .header("Authorization", "Bearer token-invalido-e-mal-formado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vendedorNaoPodeCriarCarro() throws Exception {
        CarroRequest request = new CarroRequest("Fiat", "Uno", 2015);

        mockMvc.perform(post("/carro/salvar")
                        .header("Authorization", "Bearer " + vendedorToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNaoPodeCriarUsuario() throws Exception {
        String body = "{\"email\":\"outro@teste.com\",\"password\":\"senha123\",\"role\":\"VENDEDOR\"}";

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + vendedorToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void gerentePodeCriarCarro() throws Exception {
        CarroRequest request = new CarroRequest("Fiat", "Palio", 2017);

        mockMvc.perform(post("/carro/salvar")
                        .header("Authorization", "Bearer " + gerenteToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String vendedorToken() {
        User user = userRepository.save(new User("vendedor-seguranca@teste.com", "senha", UserRole.VENDEDOR));
        return tokenProvider.generateToken(user);
    }

    private String gerenteToken() {
        User user = userRepository.save(new User("gerente-seguranca@teste.com", "senha", UserRole.GERENTE));
        return tokenProvider.generateToken(user);
    }
}
