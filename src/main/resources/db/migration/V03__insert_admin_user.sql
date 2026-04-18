INSERT INTO usuario (name, user_name, email, password, activated, force_password_change)
VALUES (
           'Procurador Geral',
           'procurador.geral',
           'pgm.ti@natal.rn.gov.br',
           '$2a$12$4yLCzYlyzktiRNvctGDv4eyH182kZBqVPRHYKyOGthb.b/2oztshu',
           TRUE,
           FALSE
       );

-- Recuperar o ID gerado para o utilizador acima e inserir a permissão
-- O LAST_INSERT_ID() pega automaticamente o ID do último insert feito nesta sessão.
SET @admin_id = LAST_INSERT_ID();

-- Assumindo que o nome da tabela auxiliar é 'usuario_permissao'
-- e que a coluna de valor se chama 'permissao' (conforme o teu @Column)
INSERT INTO usuario_permissao (usuario_id, permissao)
VALUES (@admin_id, 'admin');

-- Se quiseres adicionar mais permissões para o mesmo Admin, basta repetir:
-- INSERT INTO usuario_permissao (usuario_id, permissao) VALUES (@admin_id, 'rh');