package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServidorRepository extends JpaRepository<Servidor, Integer> {
    boolean existsByCpf(String cpf);
    boolean existsByMatricula(String matricula);

    Optional<Servidor> findByCpf(String cpf);
    Optional<Servidor> findByMatricula(String matricula);
}
