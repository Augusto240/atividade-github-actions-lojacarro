package br.org.edu.ifrn.lojacarro.cliente;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsuarioClientTest {

    private MockRestServiceServer servidor;
    private UsuarioClient usuarioClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        usuarioClient = new UsuarioClient(builder, "http://usuarios");
    }

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void devolveONomeQueAApiMandou() {
        servidor.expect(requestTo("http://usuarios/usuarios/2/nome"))
                .andRespond(withSuccess("{\"nome\":\"Maria\"}", MediaType.APPLICATION_JSON));

        assertEquals(Optional.of("Maria"), usuarioClient.buscarNome(2L));
        servidor.verify();
    }

    @Test
    void repassaORequestIdPraOutraApi() {
        MDC.put("requestId", "web-abc123");
        servidor.expect(requestTo("http://usuarios/usuarios/2/nome"))
                .andExpect(header("X-Request-Id", "web-abc123"))
                .andRespond(withSuccess("{\"nome\":\"Maria\"}", MediaType.APPLICATION_JSON));

        usuarioClient.buscarNome(2L);

        servidor.verify();
    }

    @Test
    void usuarioQueNaoExisteVoltaVazio() {
        servidor.expect(requestTo("http://usuarios/usuarios/99/nome"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertEquals(Optional.empty(), usuarioClient.buscarNome(99L));
    }

    @Test
    void apiComErroVira503() {
        servidor.expect(requestTo("http://usuarios/usuarios/2/nome"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioClient.buscarNome(2L));

        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    void apiForaDoArVira503() {
        servidor.expect(requestTo("http://usuarios/usuarios/2/nome"))
                .andRespond(withException(new ConnectException("Connection refused")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioClient.buscarNome(2L));

        assertEquals(503, ex.getStatusCode().value());
    }
}
