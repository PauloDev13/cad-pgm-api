package br.gov.rn.natal.cadpgmapi.entity;

import br.gov.rn.natal.cadpgmapi.enums.TipoCertificado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Entity
@Table(name = "procurador")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Procurador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_certificado", length = 10)
    private TipoCertificado tipoCertificado;

    @Column(name = "data_expedicao")
    private LocalDateTime dataExpedicao;

    @Formula("DATE_ADD(data_expedicao, INTERVAL CASE WHEN tipo_certificado = 'A1' THEN 1 WHEN tipo_certificado = 'A3' THEN 3 ELSE 0 END YEAR)")
    private LocalDateTime dataExpiracao;
}

