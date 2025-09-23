-- V2: extra constraints, indexes, triggers for Project Service
-- - Enforce code format at DB
-- - Force uppercase code on INSERT
-- - Forbid changing code on UPDATE (immutable)
-- - Add filter indexes and trigram index for name contains (case-insensitive)

BEGIN;

-- 0) (Optional) accelerate ILIKE/contains search on name via trigram
-- Requires permission to create extension. If it fails in your env, you can comment it out.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1) Projects: enforce code format directly in DB (in addition to bean validation)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chk_projects_code_format'
      AND connamespace = 'project'::regnamespace
  ) THEN
    ALTER TABLE project.projects
      ADD CONSTRAINT chk_projects_code_format
      CHECK (code ~ '^[A-Z0-9-]{3,20}$');
  END IF;
END$$;

-- 2) Projects: force uppercase on INSERT
CREATE OR REPLACE FUNCTION project.fn_projects_uppercode_ins()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  NEW.code := UPPER(NEW.code);
  RETURN NEW;
END$$;

DROP TRIGGER IF EXISTS trg_projects_upper_ins ON project.projects;
CREATE TRIGGER trg_projects_upper_ins
  BEFORE INSERT ON project.projects
  FOR EACH ROW EXECUTE FUNCTION project.fn_projects_uppercode_ins();

-- 3) Projects: forbid code changes on UPDATE (immutable policy)
CREATE OR REPLACE FUNCTION project.fn_projects_code_immutable_upd()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.code IS DISTINCT FROM OLD.code THEN
    RAISE EXCEPTION 'Project code is immutable once created'
      USING ERRCODE = '22000'; -- data exception
  END IF;
  RETURN NEW;
END$$;

DROP TRIGGER IF EXISTS trg_projects_code_immutable_upd ON project.projects;
CREATE TRIGGER trg_projects_code_immutable_upd
  BEFORE UPDATE ON project.projects
  FOR EACH ROW EXECUTE FUNCTION project.fn_projects_code_immutable_upd();

-- 4) Helpful indexes for filters
-- status filter
CREATE INDEX IF NOT EXISTS idx_projects_status
  ON project.projects (status);

-- date range filters
CREATE INDEX IF NOT EXISTS idx_projects_start_date
  ON project.projects (start_date);

CREATE INDEX IF NOT EXISTS idx_projects_end_date
  ON project.projects (end_date);

-- name contains (case-insensitive) using trigram
-- Note: requires pg_trgm; falls back gracefully if extension exists.
CREATE INDEX IF NOT EXISTS idx_projects_name_trgm
  ON project.projects
  USING GIN (LOWER(name) gin_trgm_ops);

-- 5) Member-side helpful indexes (you already have the uniques and basics in V1; keep them)
-- (These are no-ops if they already exist.)
CREATE INDEX IF NOT EXISTS idx_members_project
  ON project.project_members (project_id);

CREATE INDEX IF NOT EXISTS idx_members_employee
  ON project.project_members (employee_id);

COMMIT;
