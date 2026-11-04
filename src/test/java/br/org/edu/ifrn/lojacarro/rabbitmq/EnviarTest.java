package br.org.edu.ifrn.lojacarro.rabbitmq;

import br.org.edu.ifrn.lojacarro.dto.LogOperacaoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(OutputCaptureExtension.class)
class EnviarTest {

    private final ConnectionFactory foraDoAr = new RabbitConfig().rabbitConnectionFactory("localhost", 1, "aluno", "senha-teste");
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void rabbitForaDoArNaoDerrubaQuemChamou(CapturedOutput output) {
        Enviar enviar = new Enviar(foraDoAr, objectMapper, "augusto_erick");
        LogOperacaoDTO operacao = new LogOperacaoDTO("CARRO", "CARRO_CRIADO", LocalDateTime.now(), 2L, "Maria", 1L,
                "carro #1 Toyota Corolla 2024", "abc");

        assertDoesNotThrow(() -> enviar.enviarMensagem(operacao));

        assertThat(output).contains("Nao consegui mandar a mensagem para o RabbitMQ");
    }

    @Test
    void consultaDeLogsSemRabbitDa503() {
        ConsultaLogs consulta = new ConsultaLogs(foraDoAr, objectMapper, "augusto_erick");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> consulta.ultimasOperacoes(10));

        assertEquals(503, ex.getStatusCode().value());
    }
}
