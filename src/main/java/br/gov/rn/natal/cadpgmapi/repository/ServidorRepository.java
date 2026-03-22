package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServidorRepository extends JpaRepository<Servidor, Integer> {
    boolean existsByCpf(String cpf);
    boolean existsByMatricula(String matricula);

    Optional<Servidor> findByCpf(String cpf);
    Optional<Servidor> findByMatricula(String matricula);

    @Query("SELECT s FROM Servidor s WHERE " +
            "(:cpf IS NULL OR s.cpf LIKE CONCAT('%', :cpf, '%')) AND " +
            "(:matricula IS NULL OR LOWER(s.matricula) LIKE LOWER(CONCAT('%', :matricula, '%'))) AND " +
            "(:statusId IS NULL OR s.status.id =:statusId)")
    Page<Servidor> findByFilterDynamic(
            @Param("cpf") String cpf,
            @Param("matricula") String matricula,
            @Param("statusId") Integer statusId,
            Pageable pageable
    );
}
