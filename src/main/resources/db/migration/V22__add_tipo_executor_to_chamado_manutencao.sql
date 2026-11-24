-- Adiciona colunas para controle do executor da demanda (Equipe própria, Equipe contratada, Outros)
ALTER TABLE `chamado_manutencao`
    ADD COLUMN `tipo_executor` VARCHAR(30) NULL AFTER `responsavel_id`,
    ADD COLUMN `outro_executor` VARCHAR(255) NULL AFTER `tipo_executor`;
ALTER TABLE `chamado_manutencao`
    ADD CONSTRAINT `chk_chamado_tipo_executor`
        CHECK (`tipo_executor` IS NULL OR `tipo_executor` IN ('EQUIPE_PROPRIA', 'EQUIPE_CONTRATADA', 'OUTROS'));