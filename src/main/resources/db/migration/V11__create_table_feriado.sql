CREATE TABLE `feriado` (
    `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `data` VARCHAR(5) NOT NULL, -- Formato 'MM-dd'
    `nome` VARCHAR(150) NOT NULL,
    `tipo` VARCHAR(30) NOT NULL DEFAULT 'FERIADO_NACIONAL',
    `ativo` BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_feriado_data UNIQUE (`data`)
);
CREATE INDEX idx_feriado_data ON feriado(`data`);
CREATE INDEX idx_feriado_ativo ON feriado(`ativo`);
