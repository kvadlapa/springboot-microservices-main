-- V1: base objects in schema 'project' (created by Flyway)

CREATE TABLE IF NOT EXISTS project.projects (
  id           BIGSERIAL PRIMARY KEY,
  code         VARCHAR(20)  NOT NULL,
  name         VARCHAR(120) NOT NULL,
  description  VARCHAR(2000),
  status       VARCHAR(20)  NOT NULL,
  start_date   DATE         NOT NULL,
  end_date     DATE,
  CONSTRAINT chk_projects_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_projects_code_ci
  ON project.projects (LOWER(code));

CREATE TABLE IF NOT EXISTS project.project_members (
  id                 BIGSERIAL PRIMARY KEY,
  project_id         BIGINT      NOT NULL,
  employee_id        BIGINT      NOT NULL,
  role               VARCHAR(60) NOT NULL,
  allocation_percent INT         NOT NULL,
  assigned_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_alloc CHECK (allocation_percent BETWEEN 0 AND 100),
  CONSTRAINT uk_project_members_project_employee UNIQUE (project_id, employee_id),
  CONSTRAINT fk_member_project FOREIGN KEY (project_id)
    REFERENCES project.projects(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_members_project  ON project.project_members (project_id);
CREATE INDEX IF NOT EXISTS idx_members_employee ON project.project_members (employee_id);
