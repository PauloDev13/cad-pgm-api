package br.gov.rn.natal.cadpgmapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alias_email")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Alias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String email;
}
