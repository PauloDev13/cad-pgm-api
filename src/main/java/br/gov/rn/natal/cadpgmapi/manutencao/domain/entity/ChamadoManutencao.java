package br.gov.rn.natal.cadpgmapi.manutencao.domain.entity;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.PrioridadeChamado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chamado_manutencao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ChamadoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "protocolo", nullable = false, unique = true, length = 30)
    private String protocolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Enumerated(EnumType.STRING)
    @Column(name = "andar", nullable = false, length = 30)
    private AndarCodigo andar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @Column(name = "local_descricao")
    private String localDescricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaDemanda categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_servico_id", nullable = false)
    private TipoServico tipoServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private Subcategoria subcategoria;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoChamado estado = EstadoChamado.ABERTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 20)
    @Builder.Default
    private PrioridadeChamado prioridade = PrioridadeChamado.MEDIA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atribuido_em")
    private LocalDateTime atribuidoEm;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @Column(name = "prazo_conclusao")
    private LocalDate prazoConclusao;

    @Column(name = "movel_relacionado", length = 100)
    private String movelRelacionado;

    @Column(name = "numero_requisicao_ecidade", length = 50)
    private String numeroRequisicaoEcidade;
}
