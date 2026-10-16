-- 1. Adiciona coluna servidor_id na tabela usuario
ALTER TABLE usuario ADD COLUMN servidor_id INT(11) NULL UNIQUE;

-- 2. Chave estrangeira impedindo exclusão física de servidor com credencial ativa
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_servidor
    FOREIGN KEY (servidor_id) REFERENCES servidor (id) ON DELETE RESTRICT;

-- 3. Migração de dados existentes (priorizando email institucional, depois pessoal)
UPDATE usuario u
    JOIN servidor s ON LOWER(TRIM(u.email)) = LOWER(TRIM(s.email_institucional))
SET u.servidor_id = s.id
WHERE u.user_name <> 'pgmnet';

UPDATE usuario u
    JOIN servidor s ON LOWER(TRIM(u.email)) = LOWER(TRIM(s.email_pessoal))
SET u.servidor_id = s.id
WHERE u.user_name <> 'pgmnet' AND u.servidor_id IS NULL;

-- 4. Torna colunas 'name' e 'email' anuláveis na tabela usuario
ALTER TABLE usuario MODIFY COLUMN name VARCHAR(255) NULL;
ALTER TABLE usuario MODIFY COLUMN email VARCHAR(255) NULL;

-- 5. Limpa dados redundantes dos usuários vinculados a servidores
UPDATE usuario SET name = NULL, email = NULL WHERE servidor_id IS NOT NULL;

-- 6. Check constraint: contas com servidor não possuem name/email próprios; pgmnet possui
ALTER TABLE usuario ADD CONSTRAINT chk_usuario_origem_dados
    CHECK (
        (servidor_id IS NOT NULL AND name IS NULL AND email IS NULL) OR
        (servidor_id IS NULL AND name IS NOT NULL AND email IS NOT NULL)
    );
