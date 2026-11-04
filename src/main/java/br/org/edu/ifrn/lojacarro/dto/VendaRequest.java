package br.org.edu.ifrn.lojacarro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendaRequest(Long carroId, String cliente, BigDecimal valor, LocalDate dataVenda) {
}
