CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    provider VARCHAR(255),
    register_data DATETIME(6),
    last_login_at DATETIME(6),
    online BOOLEAN NOT NULL DEFAULT FALSE,
    profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
    last_activity_at DATETIME(6),
    last_seen_at DATETIME(6),
    suspended BOOLEAN NOT NULL DEFAULT FALSE,
    banned BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    time_zone_id VARCHAR(64),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL,
    category VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS contact (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255),
    email VARCHAR(255),
    message LONGTEXT,
    send_date DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS announces (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    description TEXT,
    author VARCHAR(255),
    category_offered VARCHAR(255),
    category_required VARCHAR(255),
    image_key VARCHAR(255),
    image_path VARCHAR(255),
    additional_info VARCHAR(255),
    date DATETIME(6),
    marked_as_spam BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by_admin BOOLEAN NOT NULL DEFAULT FALSE,
    moderated_at DATETIME(6),
    user_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_announces_user (user_id),
    CONSTRAINT fk_announces_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chat_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_room_user_pair (user1_id, user2_id),
    KEY idx_chat_room_user1 (user1_id),
    KEY idx_chat_room_user2 (user2_id),
    CONSTRAINT fk_chat_room_user1 FOREIGN KEY (user1_id) REFERENCES `user` (id),
    CONSTRAINT fk_chat_room_user2 FOREIGN KEY (user2_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS conversation_participant_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    muted BOOLEAN NOT NULL DEFAULT FALSE,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    reported BOOLEAN NOT NULL DEFAULT FALSE,
    reported_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_conversation_participant_settings_room_user (chat_room_id, user_id),
    KEY idx_conversation_participant_settings_user (user_id),
    KEY idx_conversation_participant_settings_room (chat_room_id),
    CONSTRAINT fk_conversation_settings_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_conversation_settings_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    system_message BOOLEAN NOT NULL DEFAULT FALSE,
    system_title VARCHAR(120),
    system_exchange_summary VARCHAR(160),
    system_status_label VARCHAR(60),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_chat_room (chat_room_id),
    KEY idx_sender (sender_id),
    KEY idx_created_at (created_at),
    KEY idx_status (status),
    CONSTRAINT fk_message_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS message_reaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    emoji VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_message_reaction_message_user (message_id, user_id),
    KEY idx_message_reaction_user (user_id),
    CONSTRAINT fk_message_reaction_message FOREIGN KEY (message_id) REFERENCES message (id),
    CONSTRAINT fk_message_reaction_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS skill_swap_proposal (
    id BIGINT NOT NULL AUTO_INCREMENT,
    announce_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    offered_skill VARCHAR(80) NOT NULL,
    requested_skill VARCHAR(80) NOT NULL,
    requester_message VARCHAR(500),
    status VARCHAR(24) NOT NULL,
    chat_room_id BIGINT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    responded_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_skill_swap_owner_status (owner_id, status),
    KEY idx_skill_swap_requester_status (requester_id, status),
    KEY idx_skill_swap_announce_requester (announce_id, requester_id),
    KEY idx_skill_swap_chat_room (chat_room_id),
    CONSTRAINT fk_skill_swap_announce FOREIGN KEY (announce_id) REFERENCES announces (id),
    CONSTRAINT fk_skill_swap_requester FOREIGN KEY (requester_id) REFERENCES `user` (id),
    CONSTRAINT fk_skill_swap_owner FOREIGN KEY (owner_id) REFERENCES `user` (id),
    CONSTRAINT fk_skill_swap_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    target_url VARCHAR(255),
    skill_swap_proposal_id BIGINT,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_notification_recipient_created (recipient_id, created_at),
    KEY idx_notification_recipient_read (recipient_id, read_at),
    KEY idx_notification_skill_swap_proposal (skill_swap_proposal_id),
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES `user` (id),
    CONSTRAINT fk_notification_skill_swap_proposal FOREIGN KEY (skill_swap_proposal_id) REFERENCES skill_swap_proposal (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS profile_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_owner_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_display_name VARCHAR(120) NOT NULL,
    content VARCHAR(200) NOT NULL,
    rating INT,
    created_at DATETIME(6) NOT NULL,
    reported BOOLEAN NOT NULL DEFAULT FALSE,
    reported_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_profile_comment_owner_created (profile_owner_id, created_at),
    KEY idx_profile_comment_author_created (author_id, created_at),
    KEY idx_profile_comment_reported_created (reported, reported_at),
    CONSTRAINT fk_profile_comment_owner FOREIGN KEY (profile_owner_id) REFERENCES `user` (id),
    CONSTRAINT fk_profile_comment_author FOREIGN KEY (author_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS video_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    space_name VARCHAR(128) NOT NULL,
    meeting_url VARCHAR(512) NOT NULL,
    meeting_code VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    last_validated_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_video_room_chat_room_active (chat_room_id, active),
    KEY idx_video_room_space_name (space_name),
    CONSTRAINT fk_video_room_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS moderation_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    target_label VARCHAR(180) NOT NULL,
    reason VARCHAR(200) NOT NULL,
    status VARCHAR(24) NOT NULL,
    resolved_by_id BIGINT,
    resolution_notes VARCHAR(400),
    created_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_report_status_created (status, created_at),
    KEY idx_report_target (target_type, target_id),
    KEY idx_report_reporter (reporter_id, created_at),
    KEY idx_report_resolved_by (resolved_by_id),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES `user` (id),
    CONSTRAINT fk_report_resolved_by FOREIGN KEY (resolved_by_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    action_type VARCHAR(40) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id BIGINT,
    target_label VARCHAR(180),
    details VARCHAR(600),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_admin_audit_created (created_at),
    KEY idx_admin_audit_admin (admin_user_id, created_at),
    CONSTRAINT fk_admin_audit_admin_user FOREIGN KEY (admin_user_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS platform_setting (
    setting_key VARCHAR(80) NOT NULL,
    label VARCHAR(120) NOT NULL,
    category VARCHAR(40) NOT NULL,
    setting_value VARCHAR(2000) NOT NULL,
    description VARCHAR(255),
    updated_by_id BIGINT,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (setting_key),
    KEY idx_platform_setting_updated_by (updated_by_id),
    CONSTRAINT fk_platform_setting_updated_by FOREIGN KEY (updated_by_id) REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS profil (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    profession VARCHAR(255),
    bio_short VARCHAR(255),
    complete_description TEXT,
    availability_mask INT NOT NULL,
    limits VARCHAR(255),
    competences TEXT,
    strengths TEXT,
    image_url VARCHAR(1000),
    reputation_score DOUBLE,
    reputation_summary TEXT,
    feedback_count_at_last_evaluation INT NOT NULL DEFAULT 0,
    user_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_profil_user (user_id),
    CONSTRAINT fk_profil_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB;
