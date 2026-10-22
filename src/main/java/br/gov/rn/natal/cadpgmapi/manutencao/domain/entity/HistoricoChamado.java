package br.gov.rn.natal.cadpgmapi.manutencao.domain.entity;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EventoHistoricoChamado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_chamado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class HistoricoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private ChamadoManutencao chamado;

    @Enumerated(EnumType.STRING)
    @Column(name = "evento", nullable = false, length = 50)
    private EventoHistoricoChamado evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoChamado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 30)
    private EstadoChamado estadoNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_anterior_id")
    private Usuario responsavelAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_novo_id")
    private Usuario responsavelNovo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ator_id", nullable = false)
    private Usuario ator;

    @CreationTimestamp
    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "observacao", length = 1000)
    private String observacao;
}
