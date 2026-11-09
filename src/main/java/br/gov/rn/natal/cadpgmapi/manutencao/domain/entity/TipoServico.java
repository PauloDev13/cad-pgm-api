package br.gov.rn.natal.cadpgmapi.manutencao.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_servico", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tipo_servico_categoria_nome", columnNames = {"categoria_id", "nome"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class TipoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaDemanda categoria;

    // Em TipoServico.java:
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategoria_id", nullable = false)
    private Subcategoria subcategoria;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "ordem_exibicao", nullable = false)
    @Builder.Default
    private Integer ordemExibicao = 0;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "inativado_por_cascata", nullable = false)
    @Builder.Default
    private boolean inativadoPorCascata = false;

    // Méthod de conveniência para obter a categoria raiz de forma segura:
    public CategoriaDemanda getCategoria() {
        return subcategoria != null ? subcategoria.getCategoria() : null;
    }
}
