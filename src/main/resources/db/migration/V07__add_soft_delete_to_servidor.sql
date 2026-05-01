-- V007__add_soft_delete_to_servidor.sql

ALTER TABLE servidor
    ADD COLUMN excluded BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN excluded_date DATETIME DEFAULT NULL;

-- Dica de Performance: Criar um índice para a busca de ativos ser instantânea
CREATE INDEX idx_servidor_excluded ON servidor(excluded);