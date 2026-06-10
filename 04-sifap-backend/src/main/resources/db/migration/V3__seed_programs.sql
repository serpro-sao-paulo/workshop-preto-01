-- ============================================================================
-- V3__seed_programs.sql — Programas sociais iniciais do SIFAP 2.0
-- ============================================================================
-- Dados migrados de CADPROG.NSN / PROGRAMA-SOCIAL.ddm (FNR 151).
-- vlr_base substitui constante 0.347215 hardcoded em CALCBENF.NSN (BR-016).
-- REQ-PRG-001.
-- ============================================================================

INSERT INTO prg_social_program (cod_programa, nome, tipo, vlr_base, fator_reajuste, status_prog, dt_vigencia_ini)
VALUES
  (1001, 'Bolsa Família - Básico',          'A', 347.22, 0.0000, 'A', '1997-04-18'),
  (1002, 'Bolsa Família - Gestante',        'A', 400.00, 0.0000, 'A', '2003-01-01'),
  (1003, 'Bolsa Família - Nutriz',          'A', 380.00, 0.0000, 'A', '2003-01-01'),
  (1004, 'Bolsa Família - Criança',         'B', 142.00, 0.0000, 'A', '2003-01-01'),
  (1005, 'BPC - LOAS Idoso',                'B', 1412.00, 0.0000, 'A', '1996-01-01'),
  (1006, 'BPC - LOAS Deficiência',          'B', 1412.00, 0.0000, 'A', '1996-01-01'),
  (1007, 'Auxílio Gás',                     'C', 102.00, 0.0000, 'A', '2001-07-01'),
  (1008, 'Pé-de-Meia - Ensino Médio',       'D', 200.00, 0.0000, 'A', '2023-01-01'),
  (1009, 'Programa Criança Feliz',          'B', 250.00, 0.0000, 'A', '2016-11-10'),
  (1010, 'Auxílio Alimentação Escolar',     'C', 80.00,  0.0000, 'A', '2019-01-01');
