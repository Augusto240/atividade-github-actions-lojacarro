package br.org.edu.ifrn.lojacarro.repository;

import br.org.edu.ifrn.lojacarro.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByEmail(String email);
}
