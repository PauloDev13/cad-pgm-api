package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.dto.response.ServidorComboDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, JpaSpecificationExecutor<Usuario> {

    @EntityGraph(attributePaths = {"permissions", "servidor"})
    Optional<Usuario> findByUserName(String login);

    @NotNull
    @Override
    @EntityGraph(attributePaths = {"permissions", "servidor"})
    Optional<Usuario> findById(@NotNull Integer id);

    @NotNull
    @Override
    @EntityGraph(attributePaths = {"permissions", "servidor"})
    Page<Usuario> findAll(@NotNull Specification<Usuario> spec, @NotNull Pageable pageable);

    @Query("""
        SELECT u FROM Usuario u
        LEFT JOIN FETCH u.servidor s
        LEFT JOIN FETCH u.permissions
        WHERE (u.servidor IS NOT NULL AND (LOWER(s.emailInstitucional) = LOWER(:email) OR LOWER(s.emailPessoal) = LOWER(:email)))
           OR (u.servidor IS NULL AND LOWER(u.email) = LOWER(:email))
    """)
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca servidores ativos que ainda não possuem credencial de usuário vinculada.
     */
    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.dto.response.ServidorComboDTO(
            s.id,
            CONCAT(s.nome, ' (Matrícula: ', s.matricula, ')')
        )
        FROM Servidor s
        WHERE s.excluded = false
          AND s.usuario IS NULL
        ORDER BY s.nome ASC
    """)
    List<ServidorComboDTO> findServidoresSemUsuario();

    boolean existsByUserName(String userName);
    boolean existsByServidorId(Integer servidorId);

    @EntityGraph(attributePaths = {"permissions", "servidor"})
    Optional<Usuario> findByServidorId(Integer servidorId);
}
