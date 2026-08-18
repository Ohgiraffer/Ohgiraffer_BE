CREATE TABLE ai_usage_log (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              feature_name VARCHAR(50) NOT NULL,
                              model_name VARCHAR(50) NOT NULL,
                              input_tokens INT NOT NULL DEFAULT 0,
                              output_tokens INT NOT NULL DEFAULT 0,
                              cached_tokens INT NOT NULL DEFAULT 0,
                              total_tokens INT NOT NULL DEFAULT 0,
                              success BOOLEAN NOT NULL DEFAULT TRUE,
                              fail_reason VARCHAR(30) NULL,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              INDEX idx_feature_created (feature_name, created_at),
                              INDEX idx_created (created_at)
);