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
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_name", nullable = false, unique = true, length = 30)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column()
    private boolean activated = true;

    @CreationTimestamp
    @Column(name = "data_created")
    private LocalDateTime dataCreated;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuario_permissao",
            joinColumns = @JoinColumn(name= "usuario_id")
    )
    @Column(name = "permissao", nullable = false, length = 100)
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Column(name = "force_password_change")
    private boolean forcePasswordChange = false;

    // =========================================================================
    // MÉTODOS OBRIGATÓRIOS DO SPRING SECURITY (O PASSAPORTE)
    // =========================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission))
                .toList();
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
