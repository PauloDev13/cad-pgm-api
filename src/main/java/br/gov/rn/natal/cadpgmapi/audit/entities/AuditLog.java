package br.gov.rn.natal.cadpgmapi.audit.entities;

import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter @Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    @Column(name = "date_hour_login")
    private LocalDateTime dateHourLogin;

    @Column(name = "date_hour_action")
    private LocalDateTime dateHourAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action")
    private AuditAction typeAction;

    @Column(name = "affected_entity")
    private String affectedEntity;

    @Column(name = "id_affected_record")
    private String idAffectedRecord;

    @Column(columnDefinition = "TEXT")
    private String details;
}
