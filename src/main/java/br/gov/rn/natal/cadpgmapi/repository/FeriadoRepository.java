package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Feriado;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeriadoRepository extends BaseNameRepository<Feriado, Integer> {
    boolean existsByData(String data);

    boolean existsByDataAndIdNot(String data, Integer id);

    @Query("SELECT f FROM Feriado f WHERE f.ativo = true ORDER BY f.data ASC")
    List<Feriado> findAllAtivos();

    @Query("SELECT f FROM Feriado f WHERE f.ativo = true AND f.data LIKE :mesPrefix% ORDER BY f.data ASC")
    List<Feriado> findAtivosByMes(@Param("mesPrefix") String mesPrefix);
}
