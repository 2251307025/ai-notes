CREATE EXTENSION IF NOT EXISTS pg_search;
CREATE EXTENSION IF NOT EXISTS vector;
-- 添加向量列（维度需与嵌入模型匹配，此处以 1024 为例）
ALTER TABLE article ADD COLUMN embedding vector(1024);
-- 创建 BM25 索引，用于全文检索
CREATE INDEX idx_article_search
    ON article
        USING bm25 (id, title, content)
    WITH (key_field = 'id');
CREATE EXTENSION IF NOT EXISTS pg_search;
SELECT extversion FROM pg_extension WHERE extname = 'pg_search';

CREATE INDEX ON article USING hnsw (embedding vector_cosine_ops);