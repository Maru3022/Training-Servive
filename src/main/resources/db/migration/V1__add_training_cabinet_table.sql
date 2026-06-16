CREATE TABLE IF NOT EXISTS training_cabinets (
    id UUID PRIMARY KEY,
    correlation_id VARCHAR(128) NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_training_cabinet_correlation_id ON training_cabinets(correlation_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_training_cabinet_user_id ON training_cabinets(user_id);
