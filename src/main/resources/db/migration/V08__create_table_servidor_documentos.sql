
CREATE TABLE `servidor_documentos` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `servidor_id` INT NOT NULL,
    `origin_name` VARCHAR(255) NOT NULL,    -- Ex: "meu_diploma.pdf"
    `object_name` VARCHAR(255) NOT NULL,    -- Ex: "275/uuid-gerado.pdf" (nome no MinIO)
    `content_type` VARCHAR(50) NOT NULL,    -- Ex: "application/pdf"
    `bytes_size` BIGINT NOT NULL,           -- Para controle do RH
    `data_upload` DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_documento_servidor
    FOREIGN KEY (`servidor_id`)
    REFERENCES servidor(id) ON DELETE CASCADE
);

CREATE INDEX idx_documento_servidor_id ON servidor_documentos(`servidor_id`);