package br.org.edu.ifrn.lojacarro.dto;

import java.time.LocalDateTime;

public record OperacaoCarroDTO(
        String operacao,
        LocalDateTime dataHora,
        Long usuarioId,
        String usuarioNome,
        Long carroId,
        String marca,
        String modelo,
        int ano,
        String requestId) {
}
