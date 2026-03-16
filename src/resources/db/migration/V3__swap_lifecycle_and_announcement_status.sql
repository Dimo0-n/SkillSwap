ALTER TABLE announces
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN locked_by_swap_id BIGINT NULL,
    ADD COLUMN inactivated_reason VARCHAR(32) NULL,
    ADD COLUMN inactivated_at DATETIME(6) NULL,
    ADD KEY idx_announces_status (status),
    ADD KEY idx_announces_locked_swap (locked_by_swap_id),
    ADD CONSTRAINT fk_announces_locked_swap FOREIGN KEY (locked_by_swap_id) REFERENCES skill_swap_proposal (id);

ALTER TABLE skill_swap_proposal
    ADD COLUMN accepted_at DATETIME(6) NULL,
    ADD COLUMN started_at DATETIME(6) NULL,
    ADD COLUMN completed_at DATETIME(6) NULL,
    ADD COLUMN cancelled_at DATETIME(6) NULL,
    ADD COLUMN cancelled_by_user_id BIGINT NULL,
    ADD COLUMN cancellation_reason VARCHAR(255) NULL,
    ADD KEY idx_skill_swap_cancelled_by_user (cancelled_by_user_id),
    ADD CONSTRAINT fk_skill_swap_cancelled_by_user FOREIGN KEY (cancelled_by_user_id) REFERENCES `user` (id);

UPDATE skill_swap_proposal
SET status = 'CANCELLED'
WHERE status = 'REJECTED';

UPDATE skill_swap_proposal
SET status = 'ACCEPTED'
WHERE status = 'NEGOTIATING';

UPDATE skill_swap_proposal
SET accepted_at = COALESCE(responded_at, updated_at)
WHERE status IN ('ACCEPTED', 'IN_PROGRESS', 'COMPLETED')
  AND accepted_at IS NULL;
