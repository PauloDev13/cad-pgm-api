-- Adiciona rastreamento de inativação por efeito cascata para preservar seleções manuais
ALTER TABLE `subcategoria`
    ADD COLUMN `inativado_por_cascata` TINYINT(1) NOT NULL DEFAULT 0 AFTER `ativo`,
    ADD INDEX `idx_subcategoria_cascata` (`categoria_id`, `ativo`, `inativado_por_cascata`);

ALTER TABLE `tipo_servico`
    ADD COLUMN `inativado_por_cascata` TINYINT(1) NOT NULL DEFAULT 0 AFTER `ativo`,
    ADD INDEX `idx_tipo_servico_cascata` (`subcategoria_id`, `ativo`, `inativado_por_cascata`);