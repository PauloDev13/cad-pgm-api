-- 4. Alterações na Tabela 'chamado_manutencao'
ALTER TABLE `chamado_manutencao`
    ADD COLUMN `subcategoria_id` INT(11) NULL AFTER `categoria_id`,
    ADD COLUMN `prioridade` VARCHAR(20) NOT NULL DEFAULT 'MEDIA' AFTER `descricao`,
    ADD COLUMN `prazo_conclusao` DATE NULL AFTER `prioridade`,
    ADD COLUMN `movel_relacionado` VARCHAR(100) NULL AFTER `prazo_conclusao`,
    ADD COLUMN `numero_requisicao_ecidade` VARCHAR(50) NULL AFTER `movelRelacionado`;

-- Atualiza chamados legados com a subcategoria vinculada ao seu tipo de serviço
UPDATE `chamado_manutencao` c
    JOIN `tipo_servico` ts ON ts.id = c.tipo_servico_id
SET c.subcategoria_id = ts.subcategoria_id;

-- Torna subcategoria_id obrigatório e adiciona constraints e índices
ALTER TABLE `chamado_manutencao`
    MODIFY COLUMN `subcategoria_id` INT(11) NOT NULL,
    ADD CONSTRAINT `fk_chamado_subcategoria` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategoria` (`id`) ON DELETE RESTRICT,
    ADD CONSTRAINT `chk_chamado_prioridade` CHECK (`prioridade` IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),
    ADD INDEX `idx_chamado_prioridade` (`prioridade`),
    ADD INDEX `idx_chamado_prazo_conclusao` (`prazo_conclusao`),
    ADD INDEX `idx_chamado_subcategoria` (`subcategoria_id`);
