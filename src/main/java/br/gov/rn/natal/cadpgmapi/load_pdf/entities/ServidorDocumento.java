package br.gov.rn.natal.cadpgmapi.load_pdf.entities;

import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "servidor_documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServidorDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relacionamento N:1 com Servidor (Sempre Lazy para performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_id", nullable = false)
    private Servidor servidor;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "bytes_size", nullable = false)
    private Long bytesSize;

    @Column(name = "data_upload", updatable = false)
    @Builder.Default
    private LocalDateTime dataUpload = LocalDateTime.now();
}
