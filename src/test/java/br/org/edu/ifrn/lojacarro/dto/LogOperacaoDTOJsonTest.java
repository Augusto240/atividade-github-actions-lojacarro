package br.org.edu.ifrn.lojacarro.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ActiveProfiles("test")
class LogOperacaoDTOJsonTest {

    @Autowired
    private JacksonTester<LogOperacaoDTO> json;

    @Test
    void viraOJsonQueOModuloDeLogEspera() throws Exception {
        LogOperacaoDTO operacao = new LogOperacaoDTO("VENDAS", "VENDA_CRIADA", LocalDateTime.of(2026, 11, 4, 10, 0, 0),
                2L, "Maria Oliveira", 5L, "venda #5 do carro #1 para Ana por R$ 90000.00", "web-abc");

        assertThat(json.write(operacao)).isEqualToJson("""
                {
                  "modulo": "VENDAS",
                  "operacao": "VENDA_CRIADA",
                  "dataHora": "2026-11-04T10:00:00",
                  "usuarioId": 2,
                  "usuarioNome": "Maria Oliveira",
                  "registroId": 5,
                  "descricao": "venda #5 do carro #1 para Ana por R$ 90000.00",
                  "requestId": "web-abc"
                }
                """);
    }
}
