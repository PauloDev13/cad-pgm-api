-- Adiciona os novos atributos de certificado digital à tabela procurador
-- Colunas criadas como NULLABLE para compatibilidade com registros históricos (V05)
ALTER TABLE procurador
    ADD COLUMN tipo_certificado VARCHAR(10) NULL COMMENT 'Tipo do Certificado Digital (A1 ou A3)',
    ADD COLUMN data_expedicao DATETIME(6) NULL COMMENT 'Data e hora da emissão do certificado digital';
