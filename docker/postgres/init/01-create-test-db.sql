SELECT 'CREATE DATABASE gamematch_test_db'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'gamematch_test_db'
)\gexec
