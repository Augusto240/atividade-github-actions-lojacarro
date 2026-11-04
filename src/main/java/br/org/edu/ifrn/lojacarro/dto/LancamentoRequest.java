package br.org.edu.ifrn.lojacarro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoRequest(String descricao, String tipo, BigDecimal valor, LocalDate data) {
}
