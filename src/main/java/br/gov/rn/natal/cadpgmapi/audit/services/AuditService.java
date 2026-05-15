package br.gov.rn.natal.cadpgmapi.audit.services;

import br.gov.rn.natal.cadpgmapi.audit.dtos.AuditLogResponseDTO;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.mappers.AuditLogMapper;
import br.gov.rn.natal.cadpgmapi.audit.repositories.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AuditService {
    private final AuditLogRepository auditRepository;
    private final AuditLogMapper mapper;

    public AuditService(AuditLogRepository auditRepository, AuditLogMapper mapper) {
        this.auditRepository = auditRepository;
        this.mapper = mapper;
    }

    // Busca registros na tabela Auditoria usando filtros
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByFilters(
            String username,
            AuditAction typeAction,
            LocalDate startDate, // Recebe apenas a data (sem hora)
            LocalDate endDate,
            Pageable pageable
    ) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // 1. Filtro por Utilizador
            if (username != null && !username.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("username"), username.trim()));
            }

            // 2. Filtro por Tipo de Ação (Enum)
            if (typeAction != null) {
                predicate = cb.and(predicate, cb.equal(root.get("typeAction"), typeAction));
            }

            // 3. Filtro por Data Inicial (A partir dessa data...)
            if (startDate != null) {
                LocalDateTime start = startDate.atStartOfDay(); // 2026-05-10T00:00:00
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dateHourAction"), start));
            }

            // 4. Filtro por Data Final (...até o fim deste dia)
            if (endDate != null) {
                LocalDateTime end= endDate.atTime(LocalTime.MAX); // 2026-05-10T23:59:59.999
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dateHourAction"), end));
            }

            return predicate;
        };

        return auditRepository.findAll(spec, pageable).map(mapper::toDto);
    }
}
