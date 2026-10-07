package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.cliente.UsuarioClient;
import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import br.org.edu.ifrn.lojacarro.repository.RegistroAuditoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class AuditoriaIntegrationTest {

    private static final String HEADER_USUARIO = "X-Usuario-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistroAuditoriaRepository auditoriaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private UsuarioClient usuarioClient;

    @BeforeEach
    void setUp() {
        when(usuarioClient.buscarNome(1L)).thenReturn(Optional.of("Marta"));
    }

    @Test
    void cadastroDeCarroApareceNaTrilhaComONomeQueVeioDaApi() throws Exception {
        cadastrar("Gol");

        mockMvc.perform(get("/auditoria").header(HEADER_USUARIO, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].acao").value("CARRO_CRIADO"))
                .andExpect(jsonPath("$[0].responsavel").value("Marta (#1)"))
                .andExpect(jsonPath("$[0].hash").isNotEmpty());
    }

    @Test
    void verAuditoriaSemIdentificacaoDaUnauthorized() throws Exception {
        mockMvc.perform(get("/auditoria"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trilhaSemMexidaEstaIntegra() throws Exception {
        cadastrar("Gol");
        cadastrar("Uno");

        mockMvc.perform(get("/auditoria/integridade").header(HEADER_USUARIO, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integra").value(true));
    }

    @Test
    void alteracaoFeitaDiretoNoBancoEhDescoberta() throws Exception {
        cadastrar("Gol");
        RegistroAuditoria alvo = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();
        cadastrar("Uno");

        jdbcTemplate.update("update auditoria set detalhes = 'nada aconteceu' where id = ?", alvo.getId());
        entityManager.clear();

        mockMvc.perform(get("/auditoria/integridade").header(HEADER_USUARIO, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integra").value(false))
                .andExpect(jsonPath("$.registroAdulterado").value(alvo.getId()));
    }

    @Test
    void registroApagadoDiretoNoBancoTambemEhDescoberto() throws Exception {
        cadastrar("Gol");
        RegistroAuditoria apagado = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();
        cadastrar("Uno");
        RegistroAuditoria seguinte = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();

        jdbcTemplate.update("delete from auditoria where id = ?", apagado.getId());
        entityManager.clear();

        mockMvc.perform(get("/auditoria/integridade").header(HEADER_USUARIO, 1))
                .andExpect(jsonPath("$.integra").value(false))
                .andExpect(jsonPath("$.registroAdulterado").value(seguinte.getId()));
    }

    private void cadastrar(String modelo) throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .header(HEADER_USUARIO, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CarroRequest("Fiat", modelo, 2020))))
                .andExpect(status().isCreated());
    }
}
