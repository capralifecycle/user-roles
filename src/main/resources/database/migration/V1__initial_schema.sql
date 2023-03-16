CREATE TABLE userroles
(
  id          uuid        NOT NULL PRIMARY KEY,
  created_at  timestamptz NOT NULL,
  modified_at timestamptz NOT NULL,
  version     bigint      NOT NULL,
  data        jsonb       NOT NULL
);

CREATE UNIQUE INDEX user_role_user_id_idx ON userroles ((data->>'userId'));

-- Index for looking up user roles with a certain org id or role name
CREATE INDEX userroles_gin_index ON userroles USING GIN ((data->'userRoles'));
