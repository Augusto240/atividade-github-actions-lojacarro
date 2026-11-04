package br.org.edu.ifrn.lojacarro.integration;

import br.org.edu.ifrn.lojacarro.cliente.UsuarioClient;
import br.org.edu.ifrn.lojacarro.dto.CarroRequest;
import br.org.edu.ifrn.lojacarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.lojacarro.rabbitmq.Enviar;
import br.org.edu.ifrn.lojacarro.repository.RegistroAuditoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
@ExtendWith(OutputCaptureExtension.class)
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistroAuditoriaRepository auditoriaRepository;

    @MockitoBean
    private UsuarioClient usuarioClient;

    @MockitoBean
    private Enviar enviar;

    @Test
    void consultarCarrosSemIdentificacaoFunciona() throws Exception {
        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk());

        verifyNoInteractions(usuarioClient);
    }

    @Test
    void cadastrarSemIdentificacaoDaUnauthorized() throws Exception {
        mockMvc.perform(cadastrarUno())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Header X-Usuario-Id não informado"));
    }

    @Test
    void usuarioQueNaoExisteNaApiDeUsuariosNaoCadastra() throws Exception {
        when(usuarioClient.buscarNome(99L)).thenReturn(Optional.empty());

        mockMvc.perform(cadastrarUno().header("X-Usuario-Id", 99))
                .andExpect(status().isUnauthorized());

        verify(enviar, never()).enviarMensagem(any(LogOperacaoDTO.class));

        assertThat(auditoriaRepository.findTopByOrderByIdDesc().orElseThrow().getAcao()).isEqualTo("ACESSO_NEGADO");
    }

    @Test
    void apiDeUsuariosForaDoArDa503() throws Exception {
        when(usuarioClient.buscarNome(2L))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço de usuários indisponível"));

        mockMvc.perform(cadastrarUno().header("X-Usuario-Id", 2))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Serviço de usuários indisponível"));
    }

    @Test
    void usuarioIdentificadoCadastraEONomeVaiProLog(CapturedOutput output) throws Exception {
        when(usuarioClient.buscarNome(2L)).thenReturn(Optional.of("Maria"));

        mockMvc.perform(cadastrarUno().header("X-Usuario-Id", 2))
                .andExpect(status().isCreated());

        assertThat(output)
                .contains("[Maria]")
                .contains("Maria (#2) cadastrou o carro #");
        assertThat(auditoriaRepository.findTopByOrderByIdDesc().orElseThrow().getResponsavel()).isEqualTo("Maria (#2)");
        ArgumentCaptor<LogOperacaoDTO> enviado = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviar).enviarMensagem(enviado.capture());
        assertThat(enviado.getValue().operacao()).isEqualTo("CARRO_CRIADO");
        assertThat(enviado.getValue().usuarioId()).isEqualTo(2L);
        assertThat(enviado.getValue().usuarioNome()).isEqualTo("Maria");
        assertThat(enviado.getValue().modulo()).isEqualTo("CARRO");
        assertThat(enviado.getValue().descricao()).contains("Fiat Uno 2015");
    }

    @Test
    void consultaComIdentificacaoTambemLogaONome(CapturedOutput output) throws Exception {
        when(usuarioClient.buscarNome(2L)).thenReturn(Optional.of("Maria"));

        mockMvc.perform(get("/carro").header("X-Usuario-Id", 2))
                .andExpect(status().isOk());

        assertThat(output).contains("Maria (#2) consultou os carros");
    }

    private MockHttpServletRequestBuilder cadastrarUno() throws Exception {
        return post("/carro/salvar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CarroRequest("Fiat", "Uno", 2015)));
    }
}
