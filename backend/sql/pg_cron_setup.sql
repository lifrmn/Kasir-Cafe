-- Optional: setup pg_cron cleanup job for revoked_tokens.
-- Jalankan script ini jika instance PostgreSQL mendukung extension pg_cron.

CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.schedule(
  'cleanup-revoked-tokens-every-10-minutes',
  '*/10 * * * *',
  $$DELETE FROM revoked_tokens WHERE expires_at <= NOW();$$
)
ON CONFLICT DO NOTHING;
