-- V6: Fix SMALLINT → INTEGER for columns mapped to Java int
-- Hibernate schema-validation expects INTEGER (int4) for Java int/Integer fields.
-- num_dependentes and cod_regiao were created as SMALLINT (int2) in V1.

ALTER TABLE ben_beneficiary
    ALTER COLUMN num_dependentes TYPE INTEGER,
    ALTER COLUMN cod_regiao      TYPE INTEGER;
