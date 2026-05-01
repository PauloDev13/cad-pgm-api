package br.gov.rn.natal.cadpgmapi.audit.repositories;

import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
