package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.dto.UsuarioRequest;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.RegistroAuditoriaRepository;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegistroAuditoriaRepository auditoriaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private Usuario marta;
    private Usuario caio;

    @BeforeEach
    void setUp() {
        marta = usuarioRepository.save(new Usuario("Marta", Cargo.GERENTE));
        caio = usuarioRepository.save(new Usuario("Caio", Cargo.VENDEDOR));
    }

    @Test
    void cadastroDeUsuarioApareceNaTrilhaComQuemFez() throws Exception {
        cadastrar("Beatriz");

        mockMvc.perform(get("/auditoria").header(HEADER_USUARIO, marta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].acao").value("USUARIO_CRIADO"))
                .andExpect(jsonPath("$[0].responsavel").value("Marta #" + marta.getId() + " (GERENTE)"))
                .andExpect(jsonPath("$[0].hash").isNotEmpty());
    }

    @Test
    void vendedorNaoVeAuditoriaEATentativaFicaRegistrada() throws Exception {
        mockMvc.perform(get("/auditoria").header(HEADER_USUARIO, caio.getId()))
                .andExpect(status().isForbidden());

        RegistroAuditoria ultimo = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();
        assertThat(ultimo.getAcao()).isEqualTo("ACESSO_NEGADO");
        assertThat(ultimo.getResponsavel()).contains("Caio");
    }

    @Test
    void trilhaSemMexidaEstaIntegra() throws Exception {
        cadastrar("Beatriz");
        cadastrar("Diego");

        mockMvc.perform(get("/auditoria/integridade").header(HEADER_USUARIO, marta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integra").value(true));
    }

    @Test
    void alteracaoFeitaDiretoNoBancoEhDescoberta() throws Exception {
        cadastrar("Beatriz");
        RegistroAuditoria alvo = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();
        cadastrar("Diego");

        jdbcTemplate.update("update auditoria set detalhes = 'nada aconteceu' where id = ?", alvo.getId());
        entityManager.clear();

        mockMvc.perform(get("/auditoria/integridade").header(HEADER_USUARIO, marta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integra").value(false))
                .andExpect(jsonPath("$.registroAdulterado").value(alvo.getId()));
    }

    @Test
    void registroApagadoDiretoNoBancoTambemEhDescoberto() throws Exception {
        cadastrar("Beatriz");
        RegistroAuditoria apagado = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();
        cadastrar("Diego");
        RegistroAuditoria seguinte = auditoriaRepository.findTopByOrderByIdDesc().orElseThrow();

        jdbcTemplate.update("delete from auditoria where id = ?", apagado.getId());
        entityManager.clear();

        mockMvc.perform(get("/auditoria/integridade").header(HEADER_USUARIO, marta.getId()))
                .andExpect(jsonPath("$.integra").value(false))
                .andExpect(jsonPath("$.registroAdulterado").value(seguinte.getId()));
    }

    private void cadastrar(String nome) throws Exception {
        mockMvc.perform(post("/usuarios")
                        .header(HEADER_USUARIO, marta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioRequest(nome, "VENDEDOR"))))
                .andExpect(status().isCreated());
    }
}
