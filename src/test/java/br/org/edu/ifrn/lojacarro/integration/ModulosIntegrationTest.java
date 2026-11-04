package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.cliente.UsuarioClient;
import br.org.edu.ifrn.lojacarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.lojacarro.rabbitmq.ConsultaLogs;
import br.org.edu.ifrn.lojacarro.rabbitmq.Enviar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
class ModulosIntegrationTest {

    private static final String HEADER = "X-Usuario-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioClient usuarioClient;

    @MockitoBean
    private Enviar enviar;

    @MockitoBean
    private ConsultaLogs consultaLogs;

    @BeforeEach
    void setUp() {
        when(usuarioClient.buscarNome(1L)).thenReturn(Optional.of("Marta"));
    }

    @Test
    void crudDeVendas() throws Exception {
        String id = criar("/vendas", "{\"carroId\":1,\"cliente\":\"Ana\",\"valor\":85000.50,\"dataVenda\":\"2026-11-04\"}");

        mockMvc.perform(get("/vendas/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Ana"))
                .andExpect(jsonPath("$.valor").value(85000.50));
        mockMvc.perform(put("/vendas/" + id).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carroId\":2,\"cliente\":\"Ana Paula\",\"valor\":80000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carroId").value(2));
        mockMvc.perform(get("/vendas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        excluir("/vendas/" + id);

        assertThat(modulosEnviados()).contains("VENDAS");
    }

    @Test
    void vendaDeCarroQueNaoExisteOuSemValorDaBadRequest() throws Exception {
        mockMvc.perform(post("/vendas").header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carroId\":999,\"cliente\":\"Ana\",\"valor\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Carro 999 não existe"));
        mockMvc.perform(post("/vendas").header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carroId\":1,\"cliente\":\"Ana\",\"valor\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crudDeTestDrive() throws Exception {
        String id = criar("/test-drives",
                "{\"carroId\":1,\"cliente\":\"Bruno\",\"dataHora\":\"2026-11-10T15:30:00\",\"observacao\":\"quer ver o porta-malas\"}");

        mockMvc.perform(put("/test-drives/" + id).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carroId\":1,\"cliente\":\"Bruno\",\"dataHora\":\"2026-11-11T09:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataHora").value("2026-11-11T09:00:00"));
        mockMvc.perform(post("/test-drives").header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carroId\":1,\"cliente\":\"Bruno\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/test-drives")).andExpect(status().isOk());
        excluir("/test-drives/" + id);

        assertThat(modulosEnviados()).contains("TEST_DRIVE");
    }

    @Test
    void crudDaContabilidade() throws Exception {
        String id = criar("/contabilidade",
                "{\"descricao\":\"Venda do Corolla\",\"tipo\":\"receita\",\"valor\":120000,\"data\":\"2026-11-04\"}");

        mockMvc.perform(get("/contabilidade/" + id)).andExpect(jsonPath("$.tipo").value("RECEITA"));
        mockMvc.perform(put("/contabilidade/" + id).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Conta de luz\",\"tipo\":\"DESPESA\",\"valor\":350.90}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("DESPESA"));
        mockMvc.perform(post("/contabilidade").header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"x\",\"tipo\":\"EMPRESTIMO\",\"valor\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Tipo inválido. Use RECEITA ou DESPESA"));
        mockMvc.perform(post("/contabilidade").header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"x\",\"tipo\":\"RECEITA\",\"valor\":-5}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/contabilidade")).andExpect(status().isOk());
        excluir("/contabilidade/" + id);

        assertThat(modulosEnviados()).contains("CONTABILIDADE");
    }

    @Test
    void crudDosAdministradores() throws Exception {
        String id = criar("/adm/administradores", "{\"nome\":\"Carla\",\"email\":\"Carla@Loja.com\"}");

        mockMvc.perform(get("/adm/administradores/" + id)).andExpect(jsonPath("$.email").value("carla@loja.com"));
        mockMvc.perform(post("/adm/administradores").header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outra Carla\",\"email\":\"carla@loja.com\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/adm/administradores/" + id).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Carla Souza\",\"email\":\"carla@loja.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Carla Souza"));
        mockMvc.perform(get("/adm/administradores")).andExpect(status().isOk());
        excluir("/adm/administradores/" + id);

        assertThat(modulosEnviados()).contains("ADM");
    }

    @Test
    void admPedeOsLogsEMostraOQueOModuloDeLogRespondeu() throws Exception {
        when(consultaLogs.ultimasOperacoes(20)).thenReturn(List.of("linha 1", "linha 2"));

        mockMvc.perform(get("/adm/logs?quantidade=20").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("linha 1"))
                .andExpect(jsonPath("$.length()").value(2));

        assertThat(modulosEnviados()).contains("ADM");
    }

    @Test
    void moduloDeLogQueNaoRespondeDa504() throws Exception {
        when(consultaLogs.ultimasOperacoes(50))
                .thenThrow(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "O módulo de log não respondeu"));

        mockMvc.perform(get("/adm/logs").header(HEADER, 1))
                .andExpect(status().isGatewayTimeout());
    }

    @Test
    void semIdentificacaoNaoAlteraNadaEmNenhumModulo() throws Exception {
        for (String rota : List.of("/vendas", "/test-drives", "/contabilidade", "/adm/administradores")) {
            mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(get("/adm/logs")).andExpect(status().isUnauthorized());
    }

    @Test
    void registroQueNaoExisteDa404() throws Exception {
        for (String rota : List.of("/vendas/999", "/test-drives/999", "/contabilidade/999", "/adm/administradores/999")) {
            mockMvc.perform(get(rota)).andExpect(status().isNotFound());
        }
    }

    private String criar(String rota, String corpo) throws Exception {
        String resposta = mockMvc.perform(post(rota).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return resposta.replaceAll(".*\"id\":(\\d+).*", "$1");
    }

    private void excluir(String rota) throws Exception {
        mockMvc.perform(delete(rota).header(HEADER, 1)).andExpect(status().isNoContent());
        mockMvc.perform(get(rota)).andExpect(status().isNotFound());
    }

    private List<String> modulosEnviados() {
        ArgumentCaptor<LogOperacaoDTO> enviados = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviar, atLeastOnce()).enviarMensagem(enviados.capture());
        return enviados.getAllValues().stream().map(LogOperacaoDTO::modulo).toList();
    }
}
