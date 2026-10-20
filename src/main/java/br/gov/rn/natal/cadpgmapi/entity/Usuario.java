package br.gov.rn.natal.cadpgmapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter @Setter
@EqualsAndHashCode(of = "id") // Compara os usuários apenas pelo ID (Acaba com os loops infinitos!)
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @Setter(AccessLevel.NONE)
    private String name;

    @Column(name = "user_name", nullable = false, unique = true, length = 30)
    private String userName;

    @Column(name = "email")
    private String email;

    @Column(nullable = false)
    private String password;

    @Column()
    @Builder.Default
    private boolean activated = true;

    @CreationTimestamp
    @Column(name = "data_created")
    private LocalDateTime dataCreated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_id", unique = true)
    private Servidor servidor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuario_permissao",
            joinColumns = @JoinColumn(name= "usuario_id")
    )
    @Column(name = "permissao", nullable = false, length = 100)
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Column(name = "force_password_change")
    @Builder.Default
    private boolean forcePasswordChange = false;

    /* =========================================================================
      CALLBACK DE CICLO DE VIDA (CORREÇÃO DO TRANSIENT PROPERTY EXCEPTION)
    ============================================================================*/

    @PreRemove
    private void preRemove() {
        if (this.servidor != null) {
            this.servidor.setUsuario(null);
            this.servidor = null;
        }
    }

    /* =========================================================================
      MÉTODOS DE DELEGAÇÃO para garantir o Servidor como FONTE ÚNICA DA VERDADE
    ============================================================================*/

    public String getName() {
        return (this.servidor != null) ? this.servidor.getNome() : this.name;
    }

    public Integer getServidorId() {
        return (this.servidor != null) ? this.servidor.getId() : null;
    }

    public String getEmail() {
        if (this.servidor != null) {
            if (this.servidor.getEmailInstitucional() != null && !this.servidor.getEmailInstitucional().isBlank()) {
                return this.servidor.getEmailInstitucional();
            }
            return this.servidor.getEmailPessoal();
        }
        return this.email;
    }

    public void setSystemAccountData(String name, String email) {
        if (this.servidor != null) {
            throw new IllegalStateException("Usuários vinculados a Servidor não aceitam nome e e-mail próprios.");
        }
        this.name = name;
        this.email = email;
    }

    public boolean isSystemAdmin() {
        return "pgmnet".equalsIgnoreCase(this.userName);
    }


    /* =========================================================================
            MÉTODOS OBRIGATÓRIOS DO SPRING SECURITY (O PASSAPORTE)
    ============================================================================*/

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Se a lista de permissões estiver nula (usuário sem permissão no banco),
        // devolve uma lista vazia para não quebrar o sistema!
        if (this.permissions == null) {
            return List.of();
        }
        return this.permissions.stream().map(SimpleGrantedAuthority::new).toList();
     // return this.permissions.stream()
    //      map(permission -> new SimpleGrantedAuthority(permission))
    //      .toList();
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.activated;
    }
}
