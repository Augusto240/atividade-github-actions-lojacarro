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
class OperacaoCarroDTOJsonTest {

    @Autowired
    private JacksonTester<OperacaoCarroDTO> json;

    @Test
    void viraOJsonQueOModuloDeLogEspera() throws Exception {
        OperacaoCarroDTO operacao = new OperacaoCarroDTO("CARRO_CRIADO", LocalDateTime.of(2026, 10, 27, 7, 46, 33),
                2L, "Maria Oliveira", 1L, "Toyota", "Corolla", 2024, "rabbit-001");

        assertThat(json.write(operacao)).isEqualToJson("""
                {
                  "operacao": "CARRO_CRIADO",
                  "dataHora": "2026-10-27T07:46:33",
                  "usuarioId": 2,
                  "usuarioNome": "Maria Oliveira",
                  "carroId": 1,
                  "marca": "Toyota",
                  "modelo": "Corolla",
                  "ano": 2024,
                  "requestId": "rabbit-001"
                }
                """);
    }

    @Test
    void jsonVoltaParaOMesmoDto() throws Exception {
        OperacaoCarroDTO lido = json.parseObject("""
                {"operacao":"CARRO_EXCLUIDO","dataHora":"2026-10-27T08:00:00","usuarioId":2,
                 "usuarioNome":"Maria Oliveira","carroId":5,"marca":"Fiat","modelo":"Uno","ano":2015,"requestId":"x"}
                """);

        assertThat(lido.operacao()).isEqualTo("CARRO_EXCLUIDO");
        assertThat(lido.dataHora()).isEqualTo(LocalDateTime.of(2026, 10, 27, 8, 0, 0));
        assertThat(lido.carroId()).isEqualTo(5L);
    }
}
