package br.gov.rn.natal.cadpgmapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vinculo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vinculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String nome;
}
