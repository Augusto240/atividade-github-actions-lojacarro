package br.org.edu.ifrn.lojacarro.dto;

import java.time.LocalDateTime;

public record TestDriveRequest(Long carroId, String cliente, LocalDateTime dataHora, String observacao) {
}
