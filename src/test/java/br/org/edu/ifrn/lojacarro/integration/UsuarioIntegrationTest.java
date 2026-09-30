package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.dto.UsuarioRequest;
import br.org.edu.ifrn.lojacarro.model.Cargo;
import br.org.edu.ifrn.lojacarro.model.Usuario;
import br.org.edu.ifrn.lojacarro.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class UsuarioIntegrationTest {

    private static final String HEADER_USUARIO = "X-Usuario-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario marta;
    private Usuario caio;

    @BeforeEach
    void setUp() {
        marta = usuarioRepository.save(new Usuario("Marta", Cargo.GERENTE));
        caio = usuarioRepository.save(new Usuario("Caio", Cargo.VENDEDOR));
    }

    @Test
    void gerenteFazOCrudCompleto() throws Exception {
        String resposta = mockMvc.perform(comoGerente(post("/usuarios"), new UsuarioRequest("Beatriz", "VENDEDOR")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Beatriz"))
                .andExpect(jsonPath("$.cargo").value("VENDEDOR"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(resposta).get("id").asLong();

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beatriz"));

        mockMvc.perform(comoGerente(put("/usuarios/" + id), new UsuarioRequest("Beatriz Souza", "GERENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beatriz Souza"))
                .andExpect(jsonPath("$.cargo").value("GERENTE"));

        mockMvc.perform(delete("/usuarios/" + id).header(HEADER_USUARIO, marta.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarNaoPedeLoginNemIdentificacaoEVemEmOrdemAlfabetica() throws Exception {
        String resposta = mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode lista = objectMapper.readTree(resposta);
        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).get("nome").asText()).isEqualTo("Caio");
        assertThat(lista.get(1).get("nome").asText()).isEqualTo("Marta");
    }

    @Test
    void cadastrarSemDizerQuemEstaUsandoDaUnauthorized() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioRequest("Zé", "VENDEDOR"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Header X-Usuario-Id não informado"));
    }

    @Test
    void identificacaoComUsuarioInexistenteDaUnauthorized() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .header(HEADER_USUARIO, 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioRequest("Zé", "VENDEDOR"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void identificacaoQueNaoEhNumeroDaBadRequest() throws Exception {
        mockMvc.perform(delete("/usuarios/" + caio.getId()).header(HEADER_USUARIO, "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void vendedorSoConsultaENaoAlteraNada() throws Exception {
        mockMvc.perform(comoVendedor(post("/usuarios"), new UsuarioRequest("Zé", "VENDEDOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("O cargo VENDEDOR não pode cadastrar usuários"));

        mockMvc.perform(comoVendedor(put("/usuarios/" + marta.getId()), new UsuarioRequest("Marta", "VENDEDOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/usuarios/" + marta.getId()).header(HEADER_USUARIO, caio.getId()))
                .andExpect(status().isForbidden());

        assertThat(usuarioRepository.count()).isEqualTo(2);
    }

    @Test
    void cargoInvalidoDaBadRequest() throws Exception {
        mockMvc.perform(comoGerente(post("/usuarios"), new UsuarioRequest("Zé", "ESTAGIARIO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cargo inválido. Use GERENTE ou VENDEDOR"));
    }

    @Test
    void naoDeixaOSistemaFicarSemGerente() throws Exception {
        mockMvc.perform(comoGerente(put("/usuarios/" + marta.getId()), new UsuarioRequest("Marta", "VENDEDOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void gerenteNaoExcluiASiMesmo() throws Exception {
        mockMvc.perform(delete("/usuarios/" + marta.getId()).header(HEADER_USUARIO, marta.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void respostaDevolveORequestIdEOLogMostraQuemFezOQue(CapturedOutput output) throws Exception {
        mockMvc.perform(comoGerente(post("/usuarios"), new UsuarioRequest("Beatriz", "VENDEDOR"))
                        .header("X-Request-Id", "teste-crud-123"))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Request-Id", "teste-crud-123"));

        assertThat(output)
                .contains("[teste-crud-123]")
                .contains("--> POST /usuarios")
                .contains("Usuario 'Beatriz' cadastrado")
                .contains("<-- POST /usuarios respondeu 201");
    }

    private MockHttpServletRequestBuilder comoGerente(MockHttpServletRequestBuilder builder, UsuarioRequest corpo)
            throws Exception {
        return comCorpo(builder.header(HEADER_USUARIO, marta.getId()), corpo);
    }

    private MockHttpServletRequestBuilder comoVendedor(MockHttpServletRequestBuilder builder, UsuarioRequest corpo)
            throws Exception {
        return comCorpo(builder.header(HEADER_USUARIO, caio.getId()), corpo);
    }

    private MockHttpServletRequestBuilder comCorpo(MockHttpServletRequestBuilder builder, UsuarioRequest corpo)
            throws Exception {
        return builder.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(corpo));
    }
}
