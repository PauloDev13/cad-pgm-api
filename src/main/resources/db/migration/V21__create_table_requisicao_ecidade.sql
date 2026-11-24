-- 1. Criação da tabela para armazenar as requisições do sistema e-Cidade vinculadas ao chamado (1:N)
CREATE TABLE `requisicao_ecidade` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `chamado_id` INT(11) NOT NULL,
  `numero_requisicao` VARCHAR(50) NOT NULL,
  `criado_em` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_req_ecidade_chamado` FOREIGN KEY (`chamado_id`) REFERENCES `chamado_manutencao` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `uk_req_ecidade_chamado_numero` UNIQUE (`chamado_id`, `numero_requisicao`),
  INDEX `idx_req_ecidade_chamado` (`chamado_id`),
  INDEX `idx_req_ecidade_numero` (`numero_requisicao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Backfill: Migra os números de requisição já cadastrados na coluna escalar para a nova tabela
INSERT INTO `requisicao_ecidade` (`chamado_id`, `numero_requisicao`, `criado_em`)
SELECT
    c.id,
    TRIM(c.numero_requisicao_ecidade),
    COALESCE(c.concluido_em, c.criado_em)
FROM `chamado_manutencao` c
WHERE c.numero_requisicao_ecidade IS NOT NULL
  AND TRIM(c.numero_requisicao_ecidade) <> '';

-- 3. Exclusão da coluna escalar legada (conforme aprovação)
ALTER TABLE `chamado_manutencao` DROP COLUMN `numero_requisicao_ecidade`;