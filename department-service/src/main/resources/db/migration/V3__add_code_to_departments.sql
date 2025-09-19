-- schema: department, table: departments (adjust if yours is different)
ALTER TABLE department.departments
    ADD COLUMN IF NOT EXISTS code VARCHAR(20);

-- backfill something for existing rows (so we can set NOT NULL safely)
UPDATE department.departments
SET code = CONCAT('D', id)
WHERE code IS NULL;

ALTER TABLE department.departments
    ALTER COLUMN code SET NOT NULL;

-- case-insensitive uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS ux_departments_code_ci
ON department.departments (LOWER(code));
