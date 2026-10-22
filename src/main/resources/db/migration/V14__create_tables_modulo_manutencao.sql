-- 1. Tabela de Salas (Localização Física)
CREATE TABLE `sala` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `numero_identificacao` VARCHAR(50) NOT NULL,
    `andar` VARCHAR(30) NOT NULL,
    `descricao` VARCHAR(255) NULL,
    `ativo` TINYINT(1) NOT NULL DEFAULT 1,
    `data_criacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    CONSTRAINT `chk_sala_andar` CHECK (`andar` IN ('TERREO', 'PRIMEIRO_ANDAR')),
    INDEX `idx_sala_andar_ativo` (`andar`, `ativo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 2. Tabela de Categorias de Demanda (Catálogo Administrável)
CREATE TABLE `categoria_demanda` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(100) NOT NULL,
    `descricao` VARCHAR(255) NULL,
    `ativo` TINYINT(1) NOT NULL DEFAULT 1,
    `ordem_exibicao` INT(11) NOT NULL DEFAULT 0,
    `data_criacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `data_atualizacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_categoria_nome` (`nome`),
    INDEX `idx_categoria_ativo_ordem` (`ativo`, `ordem_exibicao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 3. Tabela de Tipos de Serviço
CREATE TABLE `tipo_servico` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `categoria_id` INT(11) NOT NULL,
    `nome` VARCHAR(150) NOT NULL,
    `descricao` VARCHAR(255) NULL,
    `ativo` TINYINT(1) NOT NULL DEFAULT 1,
    `ordem_exibicao` INT(11) NOT NULL DEFAULT 0,
    `data_criacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `data_atualizacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_tipo_servico_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_demanda` (`id`) ON DELETE RESTRICT,
    UNIQUE KEY `uk_tipo_servico_categoria_nome` (`categoria_id`, `nome`),
    INDEX `idx_tipo_categoria_ativo_ordem` (`categoria_id`, `ativo`, `ordem_exibicao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 4. Tabela Principal de Chamados de Manutenção
CREATE TABLE `chamado_manutencao` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `protocolo` VARCHAR(30) NOT NULL,
    `solicitante_id` INT(11) NOT NULL,
    `andar` VARCHAR(30) NOT NULL,
    `sala_id` INT(11) NULL,
    `local_descricao` VARCHAR(255) NULL,
    `categoria_id` INT(11) NOT NULL,
    `tipo_servico_id` INT(11) NOT NULL,
    `descricao` TEXT NOT NULL,
    `estado` VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    `responsavel_id` INT(11) NULL,
    `criado_em` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `atribuido_em` TIMESTAMP NULL,
    `iniciado_em` TIMESTAMP NULL,
    `concluido_em` TIMESTAMP NULL,
    `cancelado_em` TIMESTAMP NULL,
    `motivo_cancelamento` VARCHAR(500) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chamado_protocolo` (`protocolo`),
    CONSTRAINT `chk_chamado_andar` CHECK (`andar` IN ('TERREO', 'PRIMEIRO_ANDAR')),
    CONSTRAINT `chk_chamado_estado` CHECK (`estado` IN ('ABERTO', 'ATRIBUIDO', 'EM_ATENDIMENTO', 'CONCLUIDO', 'CANCELADO')),
    CONSTRAINT `chk_chamado_local` CHECK (
    (`sala_id` IS NOT NULL) OR
    (`sala_id` IS NULL AND `local_descricao` IS NOT NULL AND TRIM(`local_descricao`) <> '')
    ),
    CONSTRAINT `fk_chamado_solicitante` FOREIGN KEY (`solicitante_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_chamado_sala` FOREIGN KEY (`sala_id`) REFERENCES `sala` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_chamado_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_demanda` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_chamado_tipo_servico` FOREIGN KEY (`tipo_servico_id`) REFERENCES `tipo_servico` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_chamado_responsavel` FOREIGN KEY (`responsavel_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL,
    INDEX `idx_chamado_solicitante_estado` (`solicitante_id`, `estado`),
    INDEX `idx_chamado_estado_criado` (`estado`, `criado_em`),
    INDEX `idx_chamado_responsavel_estado` (`responsavel_id`, `estado`),
    INDEX `idx_chamado_criado_em` (`criado_em`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 5. Tabela de Histórico do Chamado (Timeline Funcional Append-Only)
CREATE TABLE `historico_chamado` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `chamado_id` INT(11) NOT NULL,
    `evento` VARCHAR(50) NOT NULL,
    `estado_anterior` VARCHAR(30) NULL,
    `estado_novo` VARCHAR(30) NOT NULL,
    `responsavel_anterior_id` INT(11) NULL,
    `responsavel_novo_id` INT(11) NULL,
    `ator_id` INT(11) NOT NULL,
    `data_hora` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `observacao` VARCHAR(1000) NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_historico_chamado` FOREIGN KEY (`chamado_id`) REFERENCES `chamado_manutencao` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_historico_resp_ant` FOREIGN KEY (`responsavel_anterior_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_historico_resp_novo` FOREIGN KEY (`responsavel_novo_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_historico_ator` FOREIGN KEY (`ator_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    INDEX `idx_historico_chamado_data` (`chamado_id`, `data_hora`),
    INDEX `idx_historico_ator_data` (`ator_id`, `data_hora`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;