package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.dashboard.dto.response.GraphItemDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AniversarianteResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FolhaPontoProjectionDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.models.ServidorShadowProjection;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ServidorRepository extends JpaRepository<Servidor, Integer>,
        JpaSpecificationExecutor<Servidor> {

    /* ==================================
     CONSULTAS PARA STATUS ATIVOS
    *==================================== */

    // Substitui o soft delete
    @Modifying
    @Query("UPDATE Servidor s SET s.excluded = true, s.excludedDate = CURRENT_TIMESTAMP WHERE s.id = :id")
    void softDeleteByID(@Param("id") Integer id);

    /* ==================================
                   RELATÓRIOS
    *==================================== */

    // Busca Data de Nascimento, Nome e Setor para montar a listagem de aniversariantes do mês
    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.dto.response.AniversarianteResponseDTO(
            s.dataNascimento,
            s.nome,
            s.setor.nome
        )
        FROM Servidor s
        WHERE MONTH(s.dataNascimento) = :mes AND s.status.descricao = 'Ativo'
        ORDER BY DAY(s.dataNascimento) ASC, s.nome ASC
    """)
    // Otimiza busca no banco de dados para grandes quantidades de registros
    @QueryHints({
            @QueryHint(name = "org.hibernate.readyOnly", value = "true"),
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
            @QueryHint(name = "org.jakarta.persistence.cache.retrieveMode", value = "USE"),
            @QueryHint(name = "org.jakarta.persistence.cache.storeMode", value = "USE"),
    })
    List<AniversarianteResponseDTO>findAniversariantesDoMes(@Param("mes") Integer mes);

    // Busca os registro para montar a folha de ponto
    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.dto.response.FolhaPontoProjectionDTO(
            st.nome,
            s.nome,
            v.nome,
            s.tipoAtividade
        )
        FROM Servidor s
        JOIN s.vinculo v
        JOIN s.setor st
        JOIN s.cargo c
        WHERE s.excluded = false
        AND LOWER(v.nome) NOT IN ('terceirizado', 'terceirizado ferista', 'temporário')
        AND LOWER(c.nome) NOT IN ('procurador', 'procurador geral', 'procurador adjunto', 'chefe de procuradoria especializada')
        ORDER BY st.nome ASC, s.nome ASC
    """)
    List<FolhaPontoProjectionDTO> findAllDadosFolhaPonto();

    /* ==================================
                   DASHBOARD
    *==================================== */

    // Conta o total de servidores ativos/na base
    @Query("SELECT COUNT(s.excluded) FROM Servidor s WHERE s.excluded = false")
    Long countTotalServidoresAtivos();

    // Agrupa por Vínculo e já devolve no DTO
    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.dashboard.dto.response.GraphItemDTO(
        v.nome, COUNT(s.id))
        FROM Servidor s
        JOIN s.vinculo v
        WHERE s.excluded = false
        GROUP BY v.nome
        ORDER BY COUNT(s.id) DESC
    """)
    List<GraphItemDTO> countDistribuicaoPorVinculo();

    // Agrupa por Status e já devolve no DTO (O CAST transforma o Enum em String)
    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.dashboard.dto.response.GraphItemDTO(
        st.descricao, COUNT(s.id))
        FROM Servidor s
        JOIN s.status st
        WHERE s.excluded = false
        GROUP BY st.descricao
        ORDER BY COUNT(s.id) DESC
    """)
    List<GraphItemDTO> countDistribuicaoPorStatus();

    /* ==================================
           DOWNLOAD FOTOS
    *==================================== */
    // O nativeQuery = true faz o Hibernate ignorar o filtro de Soft Delete
    // Busca o caminho da foto no BD de qualquer Servidor independente do status
    @Query(value = "SELECT photo_path FROM servidor WHERE id = :id", nativeQuery = true)
    Optional<String>findPhotoPathByIdIgnoreStatus(@Param("id") Integer id);

    /* ==================================
     CONSULTAS PARA STATUS DESLIGADOS
    *==================================== */

    // Valida um Servidor com Status DESLIGADO pelo CPF
    @Query(value = "SELECT id, excluded, cpf, matricula FROM servidor WHERE cpf = :cpf LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkCpfStatus(@Param("cpf") String cpf);

    // Valida um Servidor com Status DESLIGADO pelo Matrícula
    @Query(value = "SELECT id, excluded, cpf, matricula FROM servidor WHERE matricula = :matricula LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkMatriculaStatus(@Param("matricula") String matricula);

    // Valida um Servidor com Status DESLIGADO pelo Email pessoal
    @Query(value = "SELECT id, excluded, email_pessoal, email_institucional " +
            "FROM servidor WHERE email_pessoal = :email LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkEmailPessoalStatus(@Param("email") String email);

    // Valida um Servidor com Status DESLIGADO pelo Email institucional
    @Query(value = "SELECT id, excluded, email_pessoal, email_institucional " +
            "FROM servidor WHERE email_institucional = :email LIMIT 1", nativeQuery = true)
    Optional<ServidorShadowProjection> checkEmailInstitucionalStatus(@Param("email") String email);

    // Busca paginada de todos os Servidores com status DESLIGADO
    @Query(value = "SELECT * FROM servidor WHERE excluded = true",
            countQuery = "SELECT count(*) FROM servidor WHERE excluded = true",
            nativeQuery = true)
    Page<Servidor> findAllExcluded(Pageable pageable);

    // Busca um Servidor com status DESLIGADO por ID
    @Query(value = "SELECT * FROM servidor WHERE excluded = true AND id = :id",
            nativeQuery = true)
    Optional<Servidor> getExcludedById(Integer id);

    // Filtragem paginada de Servidores DESLIGADOS por nome/cpf (Search da aba de excluídos)
    @Query(value = "SELECT * FROM servidor WHERE excluded = true AND (nome LIKE CONCAT('%', :term, '%') OR cpf LIKE CONCAT('%', :term, '%'))",
            countQuery = "SELECT count(*) FROM servidor WHERE excluded = true AND (nome LIKE CONCAT('%', :term, '%') OR cpf LIKE CONCAT('%', :term, '%'))",
            nativeQuery = true)
    Page<Servidor> searchExcluded(@Param("term") String term, Pageable pageable);

    // Faz atualização automática de um Servidor DESLIGADO para ATIVO (READMISSÃO)
    @Modifying
    @Query(value = "UPDATE servidor SET excluded = false, excluded_date = null WHERE id = :id", nativeQuery = true)
    void reviveNativeServidor(@Param("id") Integer id);
}
