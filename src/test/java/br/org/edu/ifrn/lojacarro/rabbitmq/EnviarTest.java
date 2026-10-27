package br.org.edu.ifrn.lojacarro.rabbitmq;

import br.org.edu.ifrn.lojacarro.dto.OperacaoCarroDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(OutputCaptureExtension.class)
class EnviarTest {

    private final Enviar enviar = new Enviar(new ObjectMapper().findAndRegisterModules(), "localhost", 1, "admin",
            "admin", "augusto");

    @Test
    void rabbitForaDoArNaoDerrubaQuemChamou(CapturedOutput output) {
        OperacaoCarroDTO operacao = new OperacaoCarroDTO("CARRO_CRIADO", LocalDateTime.now(), 2L, "Maria", 1L,
                "Toyota", "Corolla", 2024, "abc");

        assertDoesNotThrow(() -> enviar.enviarMensagem(operacao));

        assertThat(output).contains("Nao consegui mandar a mensagem para o RabbitMQ");
    }

    @Test
    void mensagemDeTextoTambemNaoDerruba() {
        assertDoesNotThrow(() -> enviar.enviarMensagem("augusto", "augusto"));
    }
}
