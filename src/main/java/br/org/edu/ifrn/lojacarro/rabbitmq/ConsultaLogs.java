package br.org.edu.ifrn.lojacarro.rabbitmq;

import br.org.edu.ifrn.lojacarro.dto.ConsultaLogsDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ConsultaLogs {

    private static final Logger log = LoggerFactory.getLogger(ConsultaLogs.class);
    private static final int ESPERA_SEGUNDOS = 5;

    private final ConnectionFactory factory;
    private final ObjectMapper objectMapper;
    private final String fila;

    public ConsultaLogs(ConnectionFactory factory, ObjectMapper objectMapper, @Value("${rabbitmq.fila}") String fila) {
        this.factory = factory;
        this.objectMapper = objectMapper;
        this.fila = fila;
    }

    public List<String> ultimasOperacoes(int quantidade) {
        String correlationId = UUID.randomUUID().toString();

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            String filaResposta = channel.queueDeclare().getQueue();
            BlockingQueue<String> resposta = new ArrayBlockingQueue<>(1);

            channel.basicConsume(filaResposta, true, (tag, entrega) -> {
                if (correlationId.equals(entrega.getProperties().getCorrelationId())) {
                    resposta.offer(new String(entrega.getBody(), StandardCharsets.UTF_8));
                }
            }, tag -> {
            });

            AMQP.BasicProperties pedido = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(filaResposta)
                    .contentType("application/json")
                    .build();
            channel.basicPublish("", fila, pedido,
                    objectMapper.writeValueAsBytes(new ConsultaLogsDTO("CONSULTA_LOGS", quantidade)));
            log.info("ADM pediu as ultimas {} operacoes ao modulo de log pela fila {}", quantidade, fila);

            String json = resposta.poll(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
            if (json == null) {
                log.warn("Modulo de log nao respondeu em {} segundos", ESPERA_SEGUNDOS);
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "O módulo de log não respondeu");
            }

            List<String> linhas = objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
            log.info("Modulo de log respondeu com {} linha(s)", linhas.size());
            return linhas;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Consulta interrompida");
        } catch (Exception e) {
            log.error("Nao consegui consultar o modulo de log ({}): {}", e.getClass().getSimpleName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível falar com o RabbitMQ");
        }
    }
}
