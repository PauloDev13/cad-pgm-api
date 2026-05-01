package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.models.ServidorShadowProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ServidorRepository extends JpaRepository<Servidor, Integer>, JpaSpecificationExecutor<Servidor> {
    // 1. O Raio-X: Retorna a Projeção (bypassa o Hibernate)
    @Query(value = "SELECT id, excluded, cpf, matricula FROM servidor WHERE cpf = :cpf LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkCpfStatus(@Param("cpf") String cpf);

    @Query(value = "SELECT id, excluded, cpf, matricula FROM servidor WHERE matricula = :matricula LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkMatriculaStatus(@Param("matricula") String matricula);

    @Query(value = "SELECT id, excluded, cpf, matricula, email_pessoal, email_institucional " +
            "FROM servidor WHERE email_pessoal = :email LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkEmailPessoalStatus(@Param("email") String email);

    @Query(value = "SELECT id, excluded, cpf, matricula, email_pessoal, email_institucional " +
            "FROM servidor WHERE email_institucional = :email LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkEmailInstitucionalStatus(@Param("email") String email);

    // Busca por Matrícula mesmo que esteja excluída
//    @Modifying
//    @Query(value = "SELECT id, excluded, cpf, matricula FROM servidor WHERE id = :id LIMIT 1", nativeQuery = true)
//    void reviveServidorNativo(@Param("id") Integer id);
//
//    // Mantemos os métodos padrão que o Spring usa (eles respeitarão o SQLRestriction)
//    boolean existsByCpf(@Param("cpf") String cpf);
//    boolean existsByMatricula(@Param("matricula") String matricula);
}
