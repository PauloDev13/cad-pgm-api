package br.gov.rn.natal.cadpgmapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "servidor")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
// 1. Substitui o DELETE físico por um UPDATE no banco
@SQLDelete(sql = "UPDATE servidor SET excluded = true, excluded_date = CURRENT_TIMESTAMP WHERE id = ?")
// 2. Filtra automaticamente todos os SELECTs para ignorar os excluídos
@SQLRestriction("excluded = false")
public class Servidor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 50, unique = true)
    private String matricula;

    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String genero;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, name = "email_pessoal", length = 100, unique = true)
    private String emailPessoal ;

    @Column(name = "email_institucional", length = 100, unique = true)
    private String emailInstitucional;
    private String endereco;
    private String filiacao;

    @Column(name = "excluded")
    private boolean excluded = false;

    @Column(name = "excluded_date")
    private LocalDateTime excludedDate;

    @Column(name = "data_desligamento")
    private LocalDate dataDesligamento ;

    // Relacionamentos N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lotacao_id")
    private Lotacao lotacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinculo_id")
    private Vinculo vinculo;

    // Relacionamentos NN mapeando as tabelas associativas do diagrama
    @ManyToMany
    @JoinTable(name = "servidor_sistema",
            joinColumns = @JoinColumn(name = "servidor_id"),
            inverseJoinColumns = @JoinColumn(name = "sistema_id"))
    @Builder.Default
    private Set<Sistema> sistemas = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "servidor_alias",
            joinColumns = @JoinColumn(name = "servidor_id"),
            inverseJoinColumns = @JoinColumn(name = "alias_id")
    )
    @Builder.Default
    private Set<Alias> aliases = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "servidor_procurador",
            joinColumns = @JoinColumn(name = "servidor_id"),
            inverseJoinColumns = @JoinColumn(name = "procurador_id")
    )
    @Builder.Default
    private Set<Procurador> procuradores = new HashSet<>();
}
