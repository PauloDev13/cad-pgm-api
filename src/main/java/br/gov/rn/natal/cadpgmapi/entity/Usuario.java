package br.gov.rn.natal.cadpgmapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario {
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

    
}
