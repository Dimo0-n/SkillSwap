ALTER TABLE skill_swap_proposal
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE skill_swap_proposal
    ADD CONSTRAINT chk_skill_swap_status
    CHECK (status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED', 'NEGOTIATING'));

CREATE TABLE IF NOT EXISTS swap_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    proposal_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(300),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_swap_review_proposal_reviewer (proposal_id, reviewer_id),
    KEY idx_swap_review_proposal_created (proposal_id, created_at),
    KEY idx_swap_review_reviewee_created (reviewee_id, created_at),
    CONSTRAINT fk_swap_review_proposal FOREIGN KEY (proposal_id) REFERENCES skill_swap_proposal (id),
    CONSTRAINT fk_swap_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES `user` (id),
    CONSTRAINT fk_swap_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES `user` (id),
    CONSTRAINT chk_swap_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;
