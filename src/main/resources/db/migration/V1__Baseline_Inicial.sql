-- Desliga a checagem de chaves estrangeiras temporariamente para evitar erros de ordem de criação
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- Tabelas Independentes (Sem Foreign Keys)
-- -----------------------------------------------------
CREATE TABLE `alias_email` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `email` varchar(150) NOT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `cargo` (
                         `id` int(11) NOT NULL AUTO_INCREMENT,
                         `nome` varchar(100) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `lotacao` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `nome` varchar(100) NOT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `procurador` (
                              `id` int(11) NOT NULL AUTO_INCREMENT,
                              `nome` varchar(150) NOT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `setor` (
                         `id` int(11) NOT NULL AUTO_INCREMENT,
                         `nome` varchar(100) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `sistema` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `nome` varchar(100) NOT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `status_servidor` (
                                   `id` int(11) NOT NULL AUTO_INCREMENT,
                                   `descricao` varchar(50) NOT NULL,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `descricao` (`descricao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `usuario` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) NOT NULL,
                           `user_name` varchar(30) NOT NULL,
                           `email` varchar(255) NOT NULL,
                           `password` varchar(255) NOT NULL,
                           `activated` tinyint(1) NOT NULL DEFAULT 1,
                           `data_created` timestamp NULL DEFAULT current_timestamp(),
                           `force_password_change` tinyint(1) DEFAULT 0,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `user_name` (`user_name`),
                           UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `vinculo` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `nome` varchar(100) NOT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- -----------------------------------------------------
-- Tabelas Dependentes (Com Foreign Keys)
-- -----------------------------------------------------
CREATE TABLE `password_reset_token` (
                                        `id` int(11) NOT NULL AUTO_INCREMENT,
                                        `token` varchar(36) NOT NULL,
                                        `usuario_id` int(11) NOT NULL,
                                        `data_expiracao` datetime NOT NULL,
                                        `usado` tinyint(1) NOT NULL DEFAULT 0,
                                        `data_criacao` timestamp NULL DEFAULT current_timestamp(),
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `token` (`token`),
                                        KEY `fk_password_reset_usuario` (`usuario_id`),
                                        CONSTRAINT `fk_password_reset_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `servidor` (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `nome` varchar(150) NOT NULL,
                            `matricula` varchar(50) NOT NULL,
                            `cpf` varchar(14) NOT NULL,
                            `data_nascimento` date DEFAULT NULL,
                            `genero` varchar(20) DEFAULT NULL,
                            `telefone` varchar(20) DEFAULT NULL,
                            `email_pessoal` varchar(100) DEFAULT NULL,
                            `email_institucional` varchar(100) DEFAULT NULL,
                            `endereco` varchar(255) DEFAULT NULL,
                            `filiacao` varchar(255) DEFAULT NULL,
                            `data_desligamento` date DEFAULT NULL,
                            `status_id` int(11) DEFAULT 1,
                            `vinculo_id` int(11) DEFAULT NULL,
                            `cargo_id` int(11) DEFAULT NULL,
                            `setor_id` int(11) DEFAULT NULL,
                            `lotacao_id` int(11) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `matricula` (`matricula`),
                            UNIQUE KEY `cpf` (`cpf`),
                            KEY `status_id` (`status_id`),
                            KEY `vinculo_id` (`vinculo_id`),
                            KEY `cargo_id` (`cargo_id`),
                            KEY `setor_id` (`setor_id`),
                            KEY `lotacao_id` (`lotacao_id`),
                            KEY `idx_servidor_nome` (`nome`),
                            CONSTRAINT `1` FOREIGN KEY (`status_id`) REFERENCES `status_servidor` (`id`),
                            CONSTRAINT `2` FOREIGN KEY (`vinculo_id`) REFERENCES `vinculo` (`id`),
                            CONSTRAINT `3` FOREIGN KEY (`cargo_id`) REFERENCES `cargo` (`id`),
                            CONSTRAINT `4` FOREIGN KEY (`setor_id`) REFERENCES `setor` (`id`),
                            CONSTRAINT `5` FOREIGN KEY (`lotacao_id`) REFERENCES `lotacao` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `servidor_alias` (
                                  `servidor_id` int(11) NOT NULL,
                                  `alias_id` int(11) NOT NULL,
                                  PRIMARY KEY (`servidor_id`,`alias_id`),
                                  KEY `alias_id` (`alias_id`),
                                  CONSTRAINT `sa_fk_1` FOREIGN KEY (`servidor_id`) REFERENCES `servidor` (`id`) ON DELETE CASCADE,
                                  CONSTRAINT `sa_fk_2` FOREIGN KEY (`alias_id`) REFERENCES `alias_email` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `servidor_procurador` (
                                       `servidor_id` int(11) NOT NULL,
                                       `procurador_id` int(11) NOT NULL,
                                       PRIMARY KEY (`servidor_id`,`procurador_id`),
                                       KEY `procurador_id` (`procurador_id`),
                                       CONSTRAINT `sp_fk_1` FOREIGN KEY (`servidor_id`) REFERENCES `servidor` (`id`) ON DELETE CASCADE,
                                       CONSTRAINT `sp_fk_2` FOREIGN KEY (`procurador_id`) REFERENCES `procurador` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `servidor_sistema` (
                                    `servidor_id` int(11) NOT NULL,
                                    `sistema_id` int(11) NOT NULL,
                                    PRIMARY KEY (`servidor_id`,`sistema_id`),
                                    KEY `sistema_id` (`sistema_id`),
                                    CONSTRAINT `ss_fk_1` FOREIGN KEY (`servidor_id`) REFERENCES `servidor` (`id`) ON DELETE CASCADE,
                                    CONSTRAINT `ss_fk_2` FOREIGN KEY (`sistema_id`) REFERENCES `sistema` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `usuario_permissao` (
                                     `usuario_id` int(11) NOT NULL,
                                     `permissao` varchar(100) NOT NULL,
                                     PRIMARY KEY (`usuario_id`,`permissao`),
                                     CONSTRAINT `fk_usuario_permissao` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- -----------------------------------------------------
-- Views
-- -----------------------------------------------------
CREATE OR REPLACE VIEW `vw_relatorio_servidores` AS
select `s`.`id` AS `servidor_id`,
       `s`.`matricula` AS `matricula`,
       `s`.`nome` AS `nome_servidor`,
       `st`.`descricao` AS `status_servidor`,
       `v`.`nome` AS `vinculo`,
       `c`.`nome` AS `cargo`,
       `setor`.`nome` AS `setor`,
       `l`.`nome` AS `lotacao`,
       `s`.`email_institucional` AS `email_institucional`,
       group_concat(distinct `sis`.`nome` order by `sis`.`nome` ASC separator ', ') AS `sistemas_acesso`,
       group_concat(distinct `p`.`nome` order by `p`.`nome` ASC separator ', ') AS `procuradores_responsaveis`,
       group_concat(distinct `ae`.`email` order by `ae`.`email` ASC separator ', ') AS `alias_email`
from (((((((((((`servidor` `s`
    left join `status_servidor` `st` on(`s`.`status_id` = `st`.`id`))
    left join `vinculo` `v` on(`s`.`vinculo_id` = `v`.`id`))
    left join `cargo` `c` on(`s`.`cargo_id` = `c`.`id`))
    left join `setor` on(`s`.`setor_id` = `setor`.`id`))
    left join `lotacao` `l` on(`s`.`lotacao_id` = `l`.`id`))
    left join `servidor_sistema` `ss` on(`s`.`id` = `ss`.`servidor_id`))
    left join `sistema` `sis` on(`ss`.`sistema_id` = `sis`.`id`))
    left join `servidor_procurador` `sp` on(`s`.`id` = `sp`.`servidor_id`))
    left join `procurador` `p` on(`sp`.`procurador_id` = `p`.`id`))
    left join `servidor_alias` `sa` on(`s`.`id` = `sa`.`servidor_id`))
    left join `alias_email` `ae` on(`sa`.`alias_id` = `ae`.`id`))
group by `s`.`id`;

-- Religa a checagem de chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 1;