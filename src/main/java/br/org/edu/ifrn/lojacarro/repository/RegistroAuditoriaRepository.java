package br.org.edu.ifrn.lojacarro.repository;

import br.org.edu.ifrn.lojacarro.model.RegistroAuditoria;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface RegistroAuditoriaRepository extends Repository<RegistroAuditoria, Long> {

    RegistroAuditoria save(RegistroAuditoria registro);

    Optional<RegistroAuditoria> findTopByOrderByIdDesc();

    List<RegistroAuditoria> findAllByOrderByIdAsc();

    List<RegistroAuditoria> findTop200ByOrderByIdDesc();

    long count();
}
