-- HUONG GIAI QUYET: Xóa bỏ các Unique Constraint cũ do Hibernate tự sinh,
-- sau đó thay thế bằng Partial Unique Index để hỗ trợ cơ chế Soft Delete.
-- Linux: docker exec -it <container_id> psql -U username -d database_name < __init__.sql
-- Powershell: Get-Content __init__.sql | docker exec -i <container_id> psql -U admin -d mydb

-------------------------------------------------------------------------------
-- 1. EmployerProfile (Unique owner_id cho 1-1)
-------------------------------------------------------------------------------
ALTER TABLE "employer_profile" DROP CONSTRAINT IF EXISTS "employer_profile_owner_id_key";
DROP INDEX IF EXISTS idx_employer_profile_owner_id;

CREATE UNIQUE INDEX idx_employer_profile_owner_id
    ON "employer_profile"("owner_id")
    WHERE "delete_at" IS NULL;

-------------------------------------------------------------------------------
-- 2. JobSeekerProfile (Unique user_id cho 1-1)
-------------------------------------------------------------------------------
ALTER TABLE "job_seeker_profile" DROP CONSTRAINT IF EXISTS "job_seeker_profile_user_id_key";
DROP INDEX IF EXISTS idx_job_seeker_profile_user_id;

CREATE UNIQUE INDEX idx_job_seeker_profile_user_id
    ON "job_seeker_profile"("user_id")
    WHERE "delete_at" IS NULL;

-------------------------------------------------------------------------------
-- 3. JobApplication (Mỗi ứng viên chỉ nộp 1 bài post 1 lần tại 1 thời điểm)
-------------------------------------------------------------------------------
ALTER TABLE "job_application" DROP CONSTRAINT IF EXISTS "job_application_job_seeker_id_job_post_id_key";
DROP INDEX IF EXISTS idx_job_application_job_seeker_job_post;

CREATE UNIQUE INDEX idx_job_application_job_seeker_job_post
    ON "job_application"("job_seeker_id", "job_post_id")
    WHERE "delete_at" IS NULL;

-------------------------------------------------------------------------------
-- 4. OAuth
-------------------------------------------------------------------------------
ALTER TABLE "oauth" DROP CONSTRAINT IF EXISTS "oauth_provider_id_key";
ALTER TABLE "oauth" DROP CONSTRAINT IF EXISTS "oauth_refresh_token_key";
DROP INDEX IF EXISTS idx_oauth_provider_id;
DROP INDEX IF EXISTS idx_oauth_refresh_token;

CREATE UNIQUE INDEX idx_oauth_provider_id
    ON "oauth"("provider_id")
    WHERE "delete_at" IS NULL;

CREATE UNIQUE INDEX idx_oauth_refresh_token
    ON "oauth"("refresh_token")
    WHERE "delete_at" IS NULL AND "refresh_token" IS NOT NULL;

-------------------------------------------------------------------------------
-- 5. SavedCandidate (Nhà tuyển dụng chỉ lưu 1 ứng viên 1 lần)
-------------------------------------------------------------------------------
ALTER TABLE "saved_candidates" DROP CONSTRAINT IF EXISTS "saved_candidates_employer_id_job_seeker_id_key";
DROP INDEX IF EXISTS idx_saved_candidates_employer_job_seeker;

CREATE UNIQUE INDEX idx_saved_candidates_employer_job_seeker
    ON "saved_candidates"("employer_id", "job_seeker_id")
    WHERE "delete_at" IS NULL;

-------------------------------------------------------------------------------
-- 6. Payment (Mã giao dịch không được trùng lặp)
-------------------------------------------------------------------------------
ALTER TABLE "payment" DROP CONSTRAINT IF EXISTS "payment_transaction_ref_key";
DROP INDEX IF EXISTS idx_payment_transaction_ref;

CREATE UNIQUE INDEX idx_payment_transaction_ref
    ON "payment"("transaction_ref")
    WHERE "delete_at" IS NULL;

-------------------------------------------------------------------------------
-- 7. Resume (Mỗi ứng viên chỉ có duy nhất 1 CV mặc định còn hoạt động)
-------------------------------------------------------------------------------
DROP INDEX IF EXISTS idx_resume_default;

CREATE UNIQUE INDEX idx_resume_default
    ON "resumes"("job_seeker_id")
    WHERE "delete_at" IS NULL AND "is_default" = true;

-------------------------------------------------------------------------------
-- 8. DeviceToken (Mỗi token của user là duy nhất)
-------------------------------------------------------------------------------
ALTER TABLE "device_tokens" DROP CONSTRAINT IF EXISTS "device_tokens_token_user_id_key";
DROP INDEX IF EXISTS idx_device_token_user;

CREATE UNIQUE INDEX idx_device_token_user
    ON "device_tokens"("token", "user_id")
    WHERE "delete_at" IS NULL;

-------------------------------------------------------------------------------
-- 9. Token (Hệ thống Token/OTP verification)
-------------------------------------------------------------------------------
ALTER TABLE "token" DROP CONSTRAINT IF EXISTS "token_token_key";
ALTER TABLE "token" DROP CONSTRAINT IF EXISTS "token_type_user_id_key";
DROP INDEX IF EXISTS idx_token_value;
DROP INDEX IF EXISTS idx_token_type_user;

-- Định danh token là duy nhất
CREATE UNIQUE INDEX idx_token_value
    ON "token"("token")
    WHERE "delete_at" IS NULL;

-- Mỗi user chỉ có 1 token active cho 1 loại hành động (Ví dụ: 1 token RESET_PASSWORD)
CREATE UNIQUE INDEX idx_token_type_user
    ON "token"("type", "user_id")
    WHERE "delete_at" IS NULL;