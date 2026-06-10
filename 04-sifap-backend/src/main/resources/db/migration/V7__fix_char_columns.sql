-- V7: Convert CHAR columns to VARCHAR
-- Hibernate schema-validation expects VARCHAR for Java String fields.
-- All CHAR columns in V1 must be VARCHAR; CHAR pads with spaces, causing bpchar vs varchar mismatch.
--
-- NOTE: PostgreSQL forbids ALTER TYPE on columns referenced by triggers.
-- trg_ben_no_cpf_change references ben_beneficiary.cpf — drop and recreate it.

-- ben_beneficiary: drop CPF-protection trigger first, then alter, then recreate
DROP TRIGGER IF EXISTS trg_ben_no_cpf_change ON ben_beneficiary;

ALTER TABLE ben_beneficiary
    ALTER COLUMN cpf  TYPE VARCHAR(11),
    ALTER COLUMN uf   TYPE VARCHAR(2),
    ALTER COLUMN nis  TYPE VARCHAR(11);

DROP TRIGGER IF EXISTS trg_ben_no_cpf_change ON ben_beneficiary;
CREATE TRIGGER trg_ben_no_cpf_change
    BEFORE UPDATE OF cpf ON ben_beneficiary
    FOR EACH ROW EXECUTE FUNCTION ben_prevent_cpf_change();

-- ben_discount
ALTER TABLE ben_discount
    ALTER COLUMN tipo_dsct TYPE VARCHAR(1);

-- prg_social_program
ALTER TABLE prg_social_program
    ALTER COLUMN tipo        TYPE VARCHAR(1),
    ALTER COLUMN status_prog TYPE VARCHAR(1);

-- pay_payment
ALTER TABLE pay_payment
    ALTER COLUMN competencia TYPE VARCHAR(7),
    ALTER COLUMN tipo_pgto   TYPE VARCHAR(1);

-- pay_payment_discount
ALTER TABLE pay_payment_discount
    ALTER COLUMN tipo_dsct TYPE VARCHAR(1);
