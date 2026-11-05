-- 3. Alteração da Tabela 'tipo_servico' para Vincular à 'subcategoria'
ALTER TABLE `tipo_servico`
    ADD COLUMN `subcategoria_id` INT(11) NULL AFTER `categoria_id`;

-- Vincula os tipos existentes à respectiva subcategoria 'Geral' da sua categoria
UPDATE `tipo_servico` ts
    JOIN `subcategoria` sc ON sc.categoria_id = ts.categoria_id AND sc.nome = 'Geral'
SET ts.subcategoria_id = sc.id;

-- Torna NOT NULL e adiciona chave estrangeira e nova UK
ALTER TABLE `tipo_servico`
    MODIFY COLUMN `subcategoria_id` INT(11) NOT NULL,
    ADD CONSTRAINT `fk_tipo_servico_subcategoria` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategoria` (`id`) ON DELETE RESTRICT,
    ADD INDEX `idx_tipo_subcategoria_ativo_ordem` (`subcategoria_id`, `ativo`, `ordem_exibicao`);
