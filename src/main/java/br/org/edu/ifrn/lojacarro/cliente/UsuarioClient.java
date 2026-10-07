package br.org.edu.ifrn.lojacarro.cliente;

import br.org.edu.ifrn.lojacarro.dto.NomeUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);

    private final RestClient restClient;

    public UsuarioClient(RestClient.Builder builder, @Value("${usuarios.api.url}") String urlBase) {
        this.restClient = builder.baseUrl(urlBase).build();
        log.info("API de usuarios configurada em {}", urlBase);
    }

    public Optional<String> buscarNome(Long id) {
        long inicio = System.currentTimeMillis();
        String requestId = MDC.get("requestId");
        try {
            NomeUsuario resposta = restClient.get()
                    .uri("/usuarios/{id}/nome", id)
                    .headers(headers -> {
                        if (requestId != null) {
                            headers.set("X-Request-Id", requestId);
                        }
                    })
                    .retrieve()
                    .body(NomeUsuario.class);
            log.info("API de usuarios devolveu o nome do usuario {} em {} ms", id, System.currentTimeMillis() - inicio);
            return Optional.ofNullable(resposta).map(NomeUsuario::nome);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("API de usuarios diz que o usuario {} nao existe", id);
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Nao consegui falar com a API de usuarios ({}): {}", e.getClass().getSimpleName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço de usuários indisponível");
        }
    }
}
