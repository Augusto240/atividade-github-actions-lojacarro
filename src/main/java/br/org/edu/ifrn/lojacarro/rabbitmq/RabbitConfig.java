package br.org.edu.ifrn.lojacarro.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    @Bean
    public ConnectionFactory rabbitConnectionFactory(@Value("${rabbitmq.host}") String host,
            @Value("${rabbitmq.porta}") int porta, @Value("${rabbitmq.usuario}") String usuario,
            @Value("${rabbitmq.senha}") String senha) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(porta);
        factory.setUsername(usuario);
        factory.setPassword(senha);
        factory.setConnectionTimeout(3000);
        log.info("RabbitMQ configurado em {}:{} com o usuario {}", host, porta, usuario);
        return factory;
    }
}
