INSERT INTO alias_email (`email`)
VALUES
('pgm.chefiaadministrativa@natal.rn.gov.br'),
('pgm.gabinete@natal.rn.gov.br'),
('pgm.chefiafiscal@natal.rn.gov.br'),
('pgm.chefiajudicial@natal.rn.gov.br'),
('pgm.dividaativa@natal.rn.gov.br'),
('pgm.sag@natal.rn.gov.br'),
('pgm.apoiofiscal@natal.rn.gov.br'),
('pgm.contabilidade@natal.rn.gov.br'),
('pgm.cartorio@natal.rn.gov.br'),
('pgm.financeiro@natal.rn.gov.br'),
('pgm.rh@natal.rn.gov.br'),
('pgm.ti@natal.rn.gov.br'),
('pgm.saude@natal.rn.gov.br'),
('pgm.licitacoes@natal.rn.gov.br'),
('pgm.rda@natal.rn.gov.br'),
('pgm.ambiental@natal.rn.gov.br'),
('pgm.patrimonial@natal.rn.gov.br'),
('selecaopgm@natal.rn.gov.br'),
('pgm.sasm@natal.rn.gov.br'),
('pgm.atendimentofiscal@natal.rn.gov.br');

INSERT INTO cargo (`nome`)
VALUES
('Analista I'),
('Analista II'),
('Analista III'),
('ASG'),
('Assessor Jurídico'),
('Chefe de Procuradoria Especializada'),
('Chefe de Setor'),
('Contador'),
('Coordenador'),
('Copeiro'),
('Diretor de Departamento'),
('Economista'),
('Encarregado de Serviço'),
('Estagiário'),
('Grupo de Nível Médio'),
('Procurador'),
('Procurador Adjunto'),
('Procurador Geral'),
('Professor');

INSERT INTO lotacao (`nome`)
VALUES
    ('PGM'),
    ('Cedido');

INSERT INTO procurador (`nome`)
VALUES
    ('Alexandre Araújo Ramos'),
    ('Aurino Lopes Vila'),
    ('Cássia Bulhões de Souza'),
    ('Celina Maria Lins Lobo'),
    ('Douglas da Costa Moreira'),
    ('Fernando Pinheiro de Sá Benevides'),
    ('Flávio de Almeida Oliveira'),
    ('George Henrique Alves de Alencar'),
    ('Hélio Messala Lima Gomes'),
    ('Herbert Alves Marinho'),
    ('Hérico Carricondes Silva de Oliveira'),
    ('Humberto Antônio Barbosa Lima'),
    ('Jamil Danilo Silva de Oliveira'),
    ('João Marcos Rodrigues de Oliveira'),
    ('Joaquim de Souza Rolim Júnior'),
    ('Luiz Antônio Ramalho de Medeiros'),
    ('Margarete Brandão Câmara'),
    ('Mariana de Souza Alves Ferreira'),
    ('Michel Franklin de Souza Figueredo'),
    ('Nair Gomes de Souza Pitombeira'),
    ('Nerival Fernandes de Araújo'),
    ('Priscilla Maria Martins Pessoa'),
    ('Railson Oliveira Bonfim'),
    ('Ramiro Oliveira de Rego Barros'),
    ('Ricardo José Bezerra de Melo Loureiro'),
    ('Suzana Cecília Cortês de Araújo'),
    ('Thiago Tavares de Queiroz'),
    ('Tiago Caetano de Souza'),
    ('Victor Holanda Chaves'),
    ('Victor Mangabeira Cruz Santos'),
    ('Rodrigo Costa Rodrigues Leite'),
    ('Vidalvo Silvino da Costa Filho');

INSERT INTO setor (`nome`)
VALUES
    ('4ª Vara da Fazenda Pública'),
    ('Almoxarifado'),
    ('Assessoria do Gabinete'),
    ('Chefia Administrativa'),
    ('Chefia de Gabinete'),
    ('Chefia Fiscal'),
    ('Chefia Judicial'),
    ('Departamento da Dívida Ativa Não Ajuizada e Cobrança Administrativa'),
    ('Departamento de Administração Geral, Licitações e Contratos'),
    ('Departamento de Apoio Fiscal'),
    ('Departamento de Cálculos e Contabilidade'),
    ('Departamento de Cartório'),
    ('Departamento de Orçamento e Finanças'),
    ('Departamento de Recursos Humanos'),
    ('Departamento de TI'),
    ('Procuradoria Administrativa'),
    ('Procuradoria da Saúde'),
    ('Procuradoria de Contratos, Licitações, Concessões e PPP'),
    ('Procuradoria de Recuperação da Dívida Ativa '),
    ('Procuradoria do Meio Ambiente'),
    ('Procuradoria Fiscal'),
    ('Procuradoria Geral'),
    ('Procuradoria Judicial'),
    ('Procuradoria Patrimonial'),
    ('Setor de Cobrança Administrativa'),
    ('Setor de Ofícios'),
    ('Procuradoria Adjunta');

INSERT INTO sistema(nome)
VALUES
    ('Directa'),
    ('eCidade'),
    ('eCidade Almoxarifado'),
    ('eDOC'),
    ('Email PMN'),
    ('INFOSEG'),
    ('MPT'),
    ('SIIG PMN'),
    ('Sistema'),
    ('STJ'),
    ('TCE RN'),
    ('TJRN 1º GRAU'),
    ('TJRN 2º GRAU'),
    ('TRE'),
    ('TRT21 1º GRAU'),
    ('TRT21 2º GRAU'),
    ('VPN');

INSERT INTO status_servidor(descricao)
VALUES
    ('Ativo'),
    ('Inativo'),
    ('Férias'),
    ('Pendente'),
    ('Afastado');

INSERT INTO vinculo (`nome`)
VALUES
    ('Comissionado'),
    ('Efetivo'),
    ('Estagiário Graduação'),
    ('Estagiário Nível Médio'),
    ('Estagiário Pós Graduação'),
    ('Residência'),
    ('Terceirizado'),
    ('Temporário'),
    ('Terceirizado Ferista');