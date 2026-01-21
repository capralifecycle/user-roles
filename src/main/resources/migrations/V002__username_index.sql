DROP INDEX "user_role_user_id_idx";

/* `UserRole.username` was previously named `userId`, but was renamed for consistency with user
   administration module. */
CREATE UNIQUE INDEX "user_role_username_idx" ON "userroles" ((data ->> 'username'));
