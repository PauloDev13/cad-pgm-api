package br.gov.rn.natal.cadpgmapi.load_pdf.repositories;

import br.gov.rn.natal.cadpgmapi.load_pdf.entities.ServidorDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServidorDocumentoRepository extends JpaRepository<ServidorDocumento, Integer> {

    // Traz todos os documentos de um servidor específico
    List<ServidorDocumento> findByServidorId(Integer servidorId);
}
