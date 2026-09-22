-- Enable pgvector and UUID extension
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Log confirmation
DO $$
BEGIN
    RAISE NOTICE 'pgvector and uuid-ossp extensions initialized successfully.';
END $$;
