package br.gov.rn.natal.cadpgmapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_servidor")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Status {
    // Descrições canônicas dos status usadas em regras de negócio e consultas.
    // (Ajuste pós-refatoração): o soft delete volta a marcar o servidor como 'Inativo'
    // (comportamento original). Centralizar aqui evita magic strings e facilita manter
    // o contrato.
    public static final String STATUS_ATIVO = "Ativo";
    public static final String STATUS_INATIVO = "Inativo";
    public static final String STATUS_PENDENTE = "Pendente";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String descricao;
}
