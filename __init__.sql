-- HUONG GIAI QUYET: Su dung mot Unique Index de tien hanh nap du lieu
-- Linux: docker exec -it <docker_image> psql -U username -d database_name < __init__.sql
-- Powershell: Get-Content __init__.sql | docker exec -i 02250ac751db psql -U admin -d mydb
CREATE UNIQUE INDEX IF NOT EXISTS idx_Employer_Profile_ownerId
    ON "employer_profile"("owner_id")
    WHERE "delete_at" is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS  idx_job_application_job_seeker_job_post
    ON "job_application"("job_seeker_id","job_post_id")
    WHERE "delete_at" is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_oauth_provider
    ON "oauth"("provider_id")
    WHERE "delete_at" is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_oauth_provider
    ON "oauth"("provider_id")
    WHERE "delete_at" is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_oauth_refresh_token
    ON "oauth"("refresh_token")
    WHERE "delete_at" is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email
    ON "users"("email")
    WHERE "delete_at" is NULL;
