-- 1. Carga de Salas Iniciais
INSERT INTO `sala` (`numero_identificacao`, `andar`, `descricao`, `ativo`) VALUES
    ('Recepção Central', 'TERREO', 'Entrada principal da PGM', 1),
    ('SAG', 'TERREO', 'Setor de Administração Geral', 1),
    ('TI', 'TERREO', 'Setor de Tecnologia da Informação', 1),
    ('Apoio Fiscal', 'TERREO', 'Apoio à Procuradoria Fiscal', 1),
    ('Almoxarifado e Suprimentos', 'TERREO', 'Setor de logística e materiais', 1),
    ('Copa/Refeitório Térreo', 'TERREO', 'Área de convivência e alimentação', 1),
    ('Auditório', 'PRIMEIRO_ANDAR', 'Auditório principal de eventos e reuniões', 1),
    ('Gabinete do Procurador Geral', 'PRIMEIRO_ANDAR', 'Gabinete central de liderança', 1),
    ('Procuradoria Fiscal', 'PRIMEIRO_ANDAR', 'Setor contencioso tributário', 1),
    ('Procuradoria Judicial', 'PRIMEIRO_ANDAR', 'Setor contencioso cível e trabalhista', 1),
    ('Procuradoria Administrativa', 'PRIMEIRO_ANDAR', 'Setor consultivo e contratos', 1),
    ('Setor de Contabilidade ', 'PRIMEIRO_ANDAR', 'Setor contencioso de cálculos', 1),
    ('Copa 1º Andar', 'PRIMEIRO_ANDAR', 'Copa de apoio do pavimento superior', 1);

-- 2. Carga das Categorias de Demanda Aprovadas
INSERT INTO `categoria_demanda` (`id`, `nome`, `ordem_exibicao`, `ativo`) VALUES
    (1, 'Limpeza e conservação', 1, 1),
    (2, 'Mobiliário', 2, 1),
    (3, 'Portas, fechaduras e janelas', 3, 1),
    (4, 'Elétrica e iluminação', 4, 1),
    (5, 'Climatização e ventilação', 5, 1),
    (6, 'Hidráulica e banheiros', 6, 1),
    (7, 'Estrutura predial', 7, 1),
    (8, 'Equipamentos e TI', 8, 1),
    (9, 'Almoxarifado e materiais', 9, 1),
    (10, 'Mudança, organização e apoio', 10, 1);

-- 3. Carga dos Tipos de Serviço por Categoria
-- 1. Limpeza e conservação
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (1, 'Limpeza extraordinária de ambiente', 1),
    (1, 'Limpeza de móveis e equipamentos', 2),
    (1, 'Limpeza de áreas comuns', 3),
    (1, 'Limpeza de copa/refeitório', 4),
    (1, 'Recolhimento de resíduos e materiais', 5),
    (1, 'Limpeza após mudança, obra ou manutenção', 6),
    (1, 'Outras demandas de limpeza', 7);

-- 2. Mobiliário
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (2, 'Manutenção/reparo de cadeira', 1),
    (2, 'Manutenção/reparo de mesa', 2),
    (2, 'Manutenção/reparo de armário ou gaveteiro', 3),
    (2, 'Movimentação ou mudança de mobiliário', 4),
    (2, 'Montagem ou desmontagem de mobiliário', 5),
    (2, 'Recolhimento de mobiliário', 6),
    (2, 'Solicitação/instalação de mobiliário', 7),
    (2, 'Adequação de layout da sala', 8),
    (2, 'Outras demandas de mobiliário', 9);

-- 3. Portas, fechaduras e janelas
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (3, 'Manutenção de porta', 1),
    (3, 'Manutenção/troca de fechadura', 2),
    (3, 'Problema com chave ou acesso', 3),
    (3, 'Manutenção de janela', 4),
    (3, 'Manutenção de persiana/cortina', 5),
    (3, 'Reparo ou substituição de vidro', 6),
    (3, 'Outras demandas de portas e acessos', 7);

-- 4. Elétrica e iluminação
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (4, 'Troca/manutenção de lâmpada ou painel LED', 1),
    (4, 'Manutenção de tomada ou interruptor', 2),
    (4, 'Problema elétrico geral', 3),
    (4, 'Adequação ou instalação de ponto elétrico', 4),
    (4, 'Problema em iluminação de emergência', 5),
    (4, 'Outras demandas elétricas', 6);

-- 5. Climatização e ventilação
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (5, 'Manutenção de ar-condicionado', 1),
    (5, 'Vazamento ou drenagem do ar-condicionado', 2),
    (5, 'Limpeza/higienização de ar-condicionado', 3),
    (5, 'Instalação ou remanejamento de ar-condicionado', 4),
    (5, 'Manutenção de ventilador', 5),
    (5, 'Instalação ou remanejamento de ventilador', 6),
    (5, 'Outras demandas de climatização', 7);

-- 6. Hidráulica e banheiros
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (6, 'Vazamento/infiltração hidráulica', 1),
    (6, 'Manutenção de torneira ou registro', 2),
    (6, 'Manutenção de pia ou ralo', 3),
    (6, 'Manutenção de vaso sanitário/descarga', 4),
    (6, 'Manutenção de dispenser e acessórios', 5),
    (6, 'Entupimento', 6),
    (6, 'Reposição de itens de higiene', 7),
    (6, 'Outras demandas hidráulicas', 8);

-- 7. Estrutura predial
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (7, 'Goteira ou infiltração', 1),
    (7, 'Problema em teto/forro', 2),
    (7, 'Problema em parede/pintura', 3),
    (7, 'Problema em piso/revestimento', 4),
    (7, 'Rachadura ou fissura', 5),
    (7, 'Problema estrutural ou risco de segurança', 6),
    (7, 'Manutenção de áreas externas', 7),
    (7, 'Outras demandas estruturais', 8);

-- 8. Equipamentos e TI
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (8, 'Problema em computador, monitor ou periférico', 1),
    (8, 'Movimentação de equipamentos de informática', 2),
    (8, 'Problema em impressora', 3),
    (8, 'Movimentação de impressora', 4),
    (8, 'Problema em telefone', 5),
    (8, 'Problema em cabeamento/ponto de rede', 6),
    (8, 'Instalação ou organização de equipamentos', 7),
    (8, 'Outras demandas de equipamentos', 8);

-- 9. Almoxarifado e materiais
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (9, 'Solicitação de material', 1),
    (9, 'Reposição de material', 2),
    (9, 'Recolhimento de material', 3),
    (9, 'Entrega de material', 4),
    (9, 'Movimentação/transferência de material', 5),
    (9, 'Controle ou substituição de mobiliário/equipamento', 6),
    (9, 'Outras demandas de almoxarifado', 7);

-- 10. Mudança, organização e apoio
INSERT INTO `tipo_servico` (`categoria_id`, `nome`, `ordem_exibicao`) VALUES
    (10, 'Mudança de setor/sala', 1),
    (10, 'Preparação de sala', 2),
    (10, 'Desocupação de sala', 3),
    (10, 'Organização de ambiente', 4),
    (10, 'Apoio para reunião/evento', 5),
    (10, 'Montagem/desmontagem de estrutura', 6),
    (10, 'Transporte interno', 7),
    (10, 'Outras demandas de apoio', 8);

-- 4. Concessão de permissões operacionais do módulo ao usuário administrador ('pgmnet')
INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'MANUTENCAO_CATALOGO_ADMIN' FROM `usuario` WHERE `user_name` = 'pgmnet';

INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'MANUTENCAO_FILA_GERAL' FROM `usuario` WHERE `user_name` = 'pgmnet';

INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'MANUTENCAO_ATENDER' FROM `usuario` WHERE `user_name` = 'pgmnet';

INSERT IGNORE INTO `usuario_permissao` (`usuario_id`, `permissao`)
SELECT `id`, 'MANUTENCAO_RELATORIOS' FROM `usuario` WHERE `user_name` = 'pgmnet';
