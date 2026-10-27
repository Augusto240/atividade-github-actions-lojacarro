package br.org.edu.ifrn.lojacarro.rabbitmq;

import br.org.edu.ifrn.lojacarro.dto.OperacaoCarroDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class Enviar {

    private static final Logger log = LoggerFactory.getLogger(Enviar.class);
    private static final AMQP.BasicProperties JSON = new AMQP.BasicProperties.Builder()
            .contentType("application/json")
            .contentEncoding("UTF-8")
            .build();

    private final ConnectionFactory factory = new ConnectionFactory();
    private final ObjectMapper objectMapper;
    private final String fila;

    public Enviar(ObjectMapper objectMapper, @Value("${rabbitmq.host}") String host,
            @Value("${rabbitmq.porta}") int porta, @Value("${rabbitmq.usuario}") String usuario,
            @Value("${rabbitmq.senha}") String senha, @Value("${rabbitmq.fila}") String fila) {
        this.objectMapper = objectMapper;
        factory.setHost(host);
        factory.setPort(porta);
        factory.setUsername(usuario);
        factory.setPassword(senha);
        factory.setConnectionTimeout(2000);
        this.fila = fila;
        log.info("RabbitMQ configurado em {}:{} na fila {}", host, porta, fila);
    }

    public void enviarMensagem(OperacaoCarroDTO operacao) {
        try {
            enviarMensagem(objectMapper.writeValueAsString(operacao), fila);
        } catch (JsonProcessingException e) {
            log.error("Nao consegui transformar a operacao em JSON: {}", e.getMessage());
        }
    }

    public void enviarMensagem(String message, String queue) {
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            channel.basicPublish("", queue, JSON, message.getBytes(StandardCharsets.UTF_8));
            log.info("Mensagem enviada para a fila {}: {}", queue, message);
        } catch (Exception e) {
            log.error("Nao consegui mandar a mensagem para o RabbitMQ ({}): {}", e.getClass().getSimpleName(),
                    e.getMessage());
        }
    }
}
