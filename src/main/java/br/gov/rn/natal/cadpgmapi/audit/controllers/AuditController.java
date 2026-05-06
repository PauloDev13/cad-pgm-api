package br.gov.rn.natal.cadpgmapi.audit.controllers;

import br.gov.rn.natal.cadpgmapi.audit.dtos.AuditLogResponseDTO;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.services.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoria", description = "API de Gestão de Auditoria")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/searchAuditFilter")
    @Operation(summary = "Filtra registros de auditoria por Username, Tipo e data da ação",
            description = "Informe os parâmetros via query parameter")
    public Page<AuditLogResponseDTO> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) AuditAction typeAction,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject @PageableDefault(
                    sort = "dateHourAction", direction = Sort.Direction.ASC) Pageable pageable) {

        return auditService.findByFilters(username, typeAction, startDate, endDate, pageable);

    }
}
