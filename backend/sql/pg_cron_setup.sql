-- Optional: setup pg_cron cleanup job for revoked_tokens.
-- Jalankan script ini jika instance PostgreSQL mendukung extension pg_cron.

CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.schedule(
  'cleanup-revoked-tokens-every-10-minutes',
  '*/10 * * * *',
  $$DELETE FROM revoked_tokens WHERE expires_at <= NOW();$$
)
ON CONFLICT DO NOTHING;

-- Retention auth audit logs: hapus log lebih lama dari 180 hari setiap hari pukul 03:00.
SELECT cron.schedule(
  'cleanup-auth-audit-logs-daily',
  '0 3 * * *',
  $$DELETE FROM auth_audit_logs WHERE created_at < NOW() - make_interval(days => 180);$$
)
ON CONFLICT DO NOTHING;
