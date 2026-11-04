package br.org.edu.ifrn.lojacarro.repository;

import br.org.edu.ifrn.lojacarro.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
}
