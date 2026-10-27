package br.org.edu.ifrn.lojacarro.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(OutputCaptureExtension.class)
class EnviarTest {

    @Test
    void rabbitForaDoArNaoDerrubaQuemChamou(CapturedOutput output) {
        Enviar enviar = new Enviar("localhost", 1, "admin", "admin", "augusto");

        assertDoesNotThrow(() -> enviar.enviarMensagem("augusto"));

        assertThat(output).contains("Nao consegui mandar a mensagem para o RabbitMQ");
    }
}
