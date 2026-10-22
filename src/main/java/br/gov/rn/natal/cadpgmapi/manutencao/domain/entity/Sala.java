package br.gov.rn.natal.cadpgmapi.manutencao.domain.entity;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sala")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_identificacao", nullable = false, length = 50)
    private String numeroIdentificacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "andar", nullable = false, length = 30)
    private AndarCodigo andar;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
