-- =========================================================================
-- V16: Inserção de Roles do Módulo de Manutenção para o Administrador Geral
-- =========================================================================

-- 1. Role para Abertura de Chamados (Solicitante)
INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'manutencao' FROM `usuario` WHERE `user_name` = 'pgmnet';

-- 2. Role para Recebimento de Atribuição (Equipe Almoxarifado)
INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'almoxarifado' FROM `usuario` WHERE `user_name` = 'pgmnet';

-- 3. Role para Gestão e Tramitação de Chamados (Tramitador)
INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'tramitador' FROM `usuario` WHERE `user_name` = 'pgmnet';
