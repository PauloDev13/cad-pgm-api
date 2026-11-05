-- 1. Criação da Tabela de Subcategorias
CREATE TABLE `subcategoria` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `categoria_id` INT(11) NOT NULL,
    `nome` VARCHAR(150) NOT NULL,
    `descricao` VARCHAR(255) NULL,
    `ativo` TINYINT(1) NOT NULL DEFAULT 1,
    `ordem_exibicao` INT(11) NOT NULL DEFAULT 0,
    `prazo_estimado_dias` INT(11) NULL,
    `requer_tombamento` TINYINT(1) NOT NULL DEFAULT 0,
    `data_criacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `data_atualizacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_subcategoria_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_demanda` (`id`) ON DELETE RESTRICT,
    UNIQUE KEY `uk_subcategoria_categoria_nome` (`categoria_id`, `nome`),
    INDEX `idx_subcategoria_categoria_ativo_ordem` (`categoria_id`, `ativo`, `ordem_exibicao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 2. Carga Inicial de Subcategorias 'Geral' para Migrar Dados Existentes
INSERT INTO `subcategoria` (`categoria_id`, `nome`, `descricao`, `ordem_exibicao`, `ativo`)
SELECT `id`, 'Geral', CONCAT('Subcategoria inicial para ', `nome`), 1, 1
FROM `categoria_demanda`;
