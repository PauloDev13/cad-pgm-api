package br.gov.rn.natal.cadpgmapi.entity;

import br.gov.rn.natal.cadpgmapi.enums.TipoFeriado;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feriado", indexes = {
        @Index(name = "idx_feriado_data", columnList = "data"),
        @Index(name = "idx_feriado_ativo", columnList = "ativo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feriado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Formato 'MM-dd' (Ex: '01-01', '04-21', '12-25')
    @Column(nullable = false, length = 5)
    private String data;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TipoFeriado tipo = TipoFeriado.FERIADO_NACIONAL;
    /**
     * Define se o feriado/ponto facultativo deve ser considerado
     * no cálculo e impressão da Folha de Ponto.
     * Default: true (Regra 4)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @PrePersist
    public void prePersist() {
        if (this.ativo == null) {
            this.ativo = true;
        }
    }
}
