-- Metadata tables owned by this service. The vector_store table itself is
-- created separately by Spring AI (spring.ai.vectorstore.pgvector.initialize-schema=true)
-- because it also needs to create the pgvector extension and an HNSW index.

CREATE TABLE ingested_documents (
    id             UUID PRIMARY KEY,
    filename       VARCHAR(1024) NOT NULL,
    content_type   VARCHAR(255)  NOT NULL,
    size_bytes     BIGINT        NOT NULL,
    chunk_count    INTEGER       NOT NULL DEFAULT 0,
    status         VARCHAR(20)   NOT NULL,
    failure_reason VARCHAR(2000),
    created_at     TIMESTAMPTZ   NOT NULL,
    ready_at       TIMESTAMPTZ
);

CREATE INDEX idx_ingested_documents_status ON ingested_documents (status);

CREATE TABLE chat_conversations (
    id               UUID PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    last_message_at  TIMESTAMPTZ  NOT NULL
);

CREATE TABLE chat_messages (
    id                UUID PRIMARY KEY,
    conversation_id   UUID          NOT NULL REFERENCES chat_conversations (id) ON DELETE CASCADE,
    role              VARCHAR(20)   NOT NULL,
    content           TEXT          NOT NULL,
    source_documents  TEXT,
    created_at        TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages (conversation_id, created_at);
