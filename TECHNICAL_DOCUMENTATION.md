# SkillSwap Technical Documentation

## 1. Introduction

### 1.1 Project Purpose
SkillSwap is a web platform that enables users to exchange skills directly with other community members. The platform addresses a practical gap in peer-to-peer learning: many users can teach one competency but need help in another. Instead of paid courses, the platform supports reciprocal learning through structured proposals, profile visibility, chat, and guided interactions.

### 1.2 Problem Statement
Traditional learning marketplaces often prioritize one-way transactions and do not optimize for mutual value exchange. SkillSwap solves this by supporting bilateral skill exchange workflows, where each participant contributes expertise and receives expertise in return.

### 1.3 Target Users
- Individual learners who want to improve practical skills
- Professionals who want to share expertise and receive mentorship in other domains
- Community members looking for collaborative, low-cost learning opportunities
- Administrators and evaluators responsible for moderation, platform quality, and operational control

### 1.4 Core Platform Concept
The platform combines public listing discovery with authenticated collaboration flows:
- Users publish announcements describing offered and requested skills
- Interested users send structured skill swap proposals
- Proposal participants negotiate and communicate in chat
- Notifications and presence improve responsiveness
- Admin controls ensure moderation and platform governance

---

## 2. System Overview

### 2.1 High-Level Architecture
SkillSwap is a monolithic Spring Boot application organized with layered architecture and modular domain responsibilities.

Main runtime components:
- Frontend: Server-side rendered Thymeleaf templates with static JavaScript and CSS assets
- Backend: Spring Boot MVC, REST, WebSocket messaging, security, and business services
- Persistence: Spring Data JPA with MySQL
- Real-time channel: STOMP over WebSocket for chat, notifications, and presence updates
- In-memory cache: Caffeine integrated through Spring Cache abstraction

### 2.2 Interaction Model
1. Browser issues HTTP requests for page rendering and REST operations.
2. Controllers validate access and delegate business work to services.
3. Services execute domain logic, call repositories, and publish events/notifications.
4. Repositories persist and query data in MySQL.
5. WebSocket broker pushes asynchronous updates for chat, notifications, and presence.
6. Read-heavy service operations are served from Caffeine caches when possible.

### 2.3 Data and Request Flow
- Synchronous flows: Authentication, profile updates, announcement management, proposal actions
- Asynchronous flows: Presence updates, WebSocket notification delivery, AI reputation refresh events
- Cached flows: Repeated reads of profiles, announcements, and categories

---

## 3. Technology Stack

### 3.1 Core Backend Stack
- Java 21: Primary implementation language
- Spring Boot: Application framework and auto-configuration
- Spring MVC and Thymeleaf: Web endpoints and server-side page rendering
- Spring Data JPA: ORM and repository abstraction
- MySQL Connector: Relational database integration
- Maven Wrapper: Build and dependency management

### 3.2 Security and Identity
- Spring Security: Authentication and authorization policy enforcement
- BCrypt: Password hashing for local accounts
- OAuth2 and OIDC Client (Google): Social login and delegated identity
- Custom UserDetails service: Role resolution and account status checks

### 3.3 Real-Time and Messaging
- Spring WebSocket with STOMP: Real-time topics and user queues
- SimpMessagingTemplate: Server-side event publishing to connected clients

### 3.4 Caching and Performance
- Caffeine: In-memory cache provider
- Spring Cache Abstraction: Declarative cache behavior at service methods
- Actuator metrics and custom admin cache API: Cache observability and runtime verification

### 3.5 Additional Integrations
- Cloudinary SDK: Profile image storage integration
- Google Meet API client: Video room creation and validation for conversations
- Spring Mail: Mail infrastructure readiness
- Validation and Jakarta APIs: Input and persistence-level constraints

### 3.6 Testing
- JUnit 5 and Spring Boot Test: Context and integration test support

---

## 4. Architecture and Layering

### 4.1 Layered Design
The application follows a classic layered structure with clear responsibilities:

1. Presentation Layer
- MVC Controllers for page rendering
- REST Controllers for API endpoints
- WebSocket controllers for chat message ingress

2. Service Layer
- Business rules, workflow orchestration, validation, and transaction boundaries
- Integration points with notification dispatching, external APIs, and cache policies
- Central location for cacheable read operations and mutation invalidation

3. Persistence Layer
- Repository interfaces per aggregate/domain
- Query methods and custom JPQL where domain filtering is needed

4. Domain Layer
- JPA entities representing users, profiles, announcements, chat, proposals, moderation, and configuration

5. Configuration Layer
- Security, WebSocket broker setup, locale, cache manager, schedulers, filters, and data bootstrap

### 4.2 Separation of Concerns
- Controllers are thin and orchestration-oriented
- Services encapsulate business logic and cross-module rules
- Repositories remain persistence-focused
- Configuration classes hold infrastructure setup only

This separation keeps functional complexity in the service layer and infrastructure concerns in dedicated configuration components.

---

## 5. Module and Component Description

### 5.1 Authentication and User Access Module
Responsibilities:
- Register users with secure password hashing
- Authenticate local and Google OAuth2/OIDC users
- Enforce account status constraints (suspended, banned, deleted)
- Track successful login metadata

Key behavior:
- Local login via email and password
- Google login with automatic first-time account provisioning
- Role assignment with ROLE_USER default and optional admin role

### 5.2 Profile Management Module
Responsibilities:
- Create and update user profile content
- Evaluate profile completion state
- Serve profile views for own and public pages
- Manage profile images
- Support profile comments and moderation reporting

Key behavior:
- Profile completion gates features such as messaging and announcement creation
- Profile comments trigger notifications and reputation refresh events
- Reputation refresh is asynchronous and uses AI-generated summary/scoring logic

### 5.3 Announcement Module
Responsibilities:
- Publish, list, retrieve, and delete announcements
- Normalize and resolve safe skill-category imagery
- Support owner-scoped listing views

Key behavior:
- Public listing pages rely on announcement service methods
- Admin actions can mark announcements as spam or delete entries

### 5.4 Category Module
Responsibilities:
- Manage reusable skill categories
- Provide category lookups for listing and domain consistency

Key behavior:
- Category data is initialized at startup when empty
- Category reads are optimized via long-lived cache regions

### 5.5 Skill Swap Proposal Module
Responsibilities:
- Create and validate skill swap proposals
- Enforce duplicate proposal constraints by status
- Process accept/reject/negotiate actions
- Open proposal-linked chat sessions

Key behavior:
- Proposal lifecycle states: PENDING, NEGOTIATING, ACCEPTED, REJECTED
- Owner actions trigger notifications and message system events
- Accepted/negotiated proposals are linked to conversation context

### 5.6 Chat and Presence Module
Responsibilities:
- Create or reuse one-to-one chat rooms
- Persist message history and reactions
- Track unread state and delivery status transitions
- Maintain conversation settings (mute, block, report)
- Broadcast user presence updates in real time

Key behavior:
- STOMP topics support message dissemination
- Presence is updated by both request filter activity and WebSocket session lifecycle
- Inactive users are moved offline by scheduler checks

### 5.7 Notification Module
Responsibilities:
- Persist user notifications with type, message, optional target URL, and proposal context
- Deliver real-time notification events to user queues
- Provide unread counts and read-state transitions
- Support platform-wide admin announcements

Key behavior:
- Notifications are generated by registration, proposals, chat events, and review actions
- Notifications include action context when tied to proposals

### 5.8 Video Meeting Module
Responsibilities:
- Create and validate Google Meet spaces for active conversations
- Persist reusable active video rooms
- Publish session-ready events to participants

Key behavior:
- Access requires authenticated user and conversation membership
- Active room validity is periodically checked by timestamp-based threshold

### 5.9 Administrative Module
Responsibilities:
- Dashboard metrics and trend views
- User moderation operations
- Skill normalization and merge tooling
- Post/review/report governance
- System notifications and media control
- Platform settings management
- Audit logging

Key behavior:
- Access restricted to administrators
- Admin operations are tracked and exposed in panel workflows
- Dedicated admin API endpoint provides cache statistics visibility

---

## 6. Data Model

### 6.1 Core Entities
- User: Account identity, credentials/provider, status flags, online metadata, roles
- Role: Authority model for authorization
- Profil: Extended user profile and reputation fields
- Announce: Published skill exchange listing
- Category: Reference taxonomy for skill domains
- SkillSwapProposal: Negotiation artifact linking requester, owner, and announcement
- ChatRoom: Direct conversation between two users
- Message: Chat content, status, and optional system metadata
- MessageReaction: Per-user emoji reaction to a message
- Notification: User-targeted event records with optional proposal relation
- ProfileComment: User feedback entries on profiles with moderation flags
- ConversationParticipantSettings: Per-user mute/block/report settings per chat room
- VideoRoom: Conversation-linked Google Meet session metadata
- Contact: Contact form persistence

### 6.2 Conceptual Relationships
- User to Role: Many-to-many
- User to Profil: One-to-many over time, latest profile used for active view
- User to Announce: One-to-many
- Announce to SkillSwapProposal: One-to-many
- SkillSwapProposal to ChatRoom: Optional many-to-one link to active negotiation/accepted conversation
- ChatRoom to Message: One-to-many
- Message to MessageReaction: One-to-many
- User to Notification: One-to-many recipient model
- ProfileComment ties author and profile owner as separate user references
- ConversationParticipantSettings ties chat room and participant preferences
- VideoRoom ties to chat room with active state semantics

### 6.3 Logical Modeling Notes
- Indexes are defined on notification, proposal, message, comment, and video room entities for access patterns
- Domain status enums support deterministic workflow transitions
- Soft moderation and account-state flags preserve auditability without destructive deletions by default

---

## 7. Caching and Performance Strategy

### 7.1 Rationale
Caching was introduced to reduce repetitive database reads for stable or moderately changing data. The objective is to improve response time and decrease load on persistence for high-frequency read paths.

### 7.2 Cache Technology
- Spring Cache abstraction for declarative service-layer caching
- Caffeine as the in-memory provider with region-level configuration

### 7.3 Cache Regions and Policies
Configured cache regions are grouped by domain:

Profiles:
- profiles.by-username
- profiles.by-user-id
- profiles.author

Announcements:
- announces.latest5
- announces.list
- announces.by-id
- announces.by-author

Categories:
- categories.all
- categories.by-id

Policy characteristics:
- Maximum size limits per region to prevent uncontrolled memory growth
- Expire-after-write settings tuned by data volatility
- Statistics enabled on all regions

Examples of policy intent:
- Frequently changing announcement lists use shorter TTLs
- Stable category data uses longer TTLs
- Profile reads use medium TTLs with targeted eviction on profile updates

### 7.4 Service-Layer Caching Scope
Caching is intentionally applied only in service read methods. Controllers and repositories are unaware of cache concerns.

Cached operations include:
- Profile retrieval by username and user ID
- Announcement listing, latest items, detail by ID, and owner lists
- Category retrieval operations

Operations explicitly excluded from caching:
- Notifications
- Real-time messaging and presence
- Highly dynamic counters and event-driven transient data

### 7.5 Invalidation Strategy
Data-modifying operations trigger invalidation:
- Profile save evicts profile-related cache entries for that user
- Announcement create/delete evicts affected list regions and detail entries
- Region-wide invalidation is used where exact key targeting is not practical for list recomposition

### 7.6 Cache Monitoring and Verification
Monitoring is supported by:
- Actuator metrics and cache exposure
- Admin cache statistics endpoint for snapshot inspection per region

Admin cache stats endpoints:
- GET /admin/api/cache/stats
- GET /admin/api/cache/stats/{cacheName}

Observed metrics include hit count, miss count, request count, hit rate, miss rate, eviction count, and estimated region size.

---

## 8. Security Model

### 8.1 Authentication
The application supports two authentication modes:
- Form-based authentication for local accounts
- OAuth2/OIDC authentication with Google

### 8.2 Authorization
Authorization is role-based:
- ROLE_USER for standard authenticated platform features
- ROLE_ADMIN for administrative panel and management operations

Access control is enforced through:
- Security filter chain URL-level rules
- Method-level annotations on sensitive controllers

### 8.3 Account Status Enforcement
Local authentication pipeline validates user state before granting access:
- Suspended accounts are blocked
- Banned accounts are blocked
- Deleted accounts are blocked

### 8.4 Sensitive Endpoint Protection
Administrative routes under /admin are restricted to ROLE_ADMIN. Additional admin API routes inherit this boundary and are also protected by explicit method-level pre-authorization.

### 8.5 Security-Related Operational Notes
- Passwords are hashed with BCrypt
- Session logout invalidates session state and presence is updated
- OAuth2 users created from provider login are assigned local authority mapping

---

## 9. API Overview

### 9.1 API Organization Principles
The backend API is organized by domain-oriented route groups, mixing MVC page routes and REST endpoints.

### 9.2 Major Endpoint Groups
- Authentication and Account:
  - Registration and login pages/actions
- Profile and User Data:
  - Profile view/edit/complete routes
  - Profile comments and moderation actions
  - User timezone update route
- Announcements and Exchange:
  - Public listing and detail routes
  - Announcement create/save/delete routes
  - Skill swap proposal API group
- Chat and Presence:
  - Chat room lifecycle and history endpoints
  - Conversation settings and unread endpoints
  - Presence ping endpoint
- Notifications:
  - Notification listing, unread count, mark-read operations
- Video Session:
  - Conversation video room creation endpoint
- Administration:
  - Admin panel routes for moderation, users, reports, settings, media, statistics
  - Admin cache stats API

### 9.3 Real-Time Endpoints
- STOMP endpoints for browser and native WebSocket clients
- Topic and user queue conventions for:
  - Chat messages
  - Notification delivery
  - Presence updates
  - Video-session readiness events

---

## 10. Notification System

### 10.1 Notification Sources
Notifications are generated by multiple domain events, including:
- Welcome onboarding after registration
- New skill swap proposals
- Proposal status changes (accepted, rejected, negotiating)
- New chat messages when recipient is not muted
- New profile comments
- Admin system broadcasts
- Conversation report actions to administrators

### 10.2 Delivery Model
1. Notification is persisted in database for durability and unread tracking.
2. Notification is mapped to DTO with optional proposal context.
3. Real-time payload is dispatched to recipient-specific WebSocket queue.
4. Client can query unread count and mark notifications as read.

### 10.3 Read-State Management
- Single notification read marking
- Bulk read marking for all unread items
- Proposal-linked read synchronization on action handling

---

## 11. Configuration and Environment

### 11.1 Configuration Sources
- application.properties for core runtime properties
- application.yml for OAuth2 scope/base URL structure
- Environment variables for secrets and integration credentials

### 11.2 Key Runtime Settings
- Spring application name
- Thymeleaf and message source configuration
- Data source and JPA behavior
- Session timeout
- OAuth2 client credentials and redirect settings
- Google Meet base URL
- Actuator exposure and cache metrics

### 11.3 Required External Configuration
Typical environment variables include:
- GOOGLE_CLIENT_ID
- GOOGLE_CLIENT_SECRET
- DEEPSEEK_API_KEY
- CLOUDINARY_URL or Cloudinary key components

### 11.4 Initialization and Bootstrap
- Roles are ensured at startup by role data loader
- Category reference data is seeded when missing

### 11.5 Localization
Locale resolver and interceptor support language switching through request parameter and message bundle files.

---

## 12. Testing and Validation

### 12.1 Current Automated Baseline
The repository includes a Spring Boot context load test to validate application startup wiring.

### 12.2 Recommended Validation Scope
For practical quality assurance, validate these areas:

Core Functional Flows:
- Local registration/login and Google login
- Profile completion gating behavior
- Announcement create/list/detail/delete lifecycle
- Proposal create and status transitions
- Chat history, reactions, and unread handling
- Notification generation and read transitions
- Admin moderation workflows

Caching Verification:
- Repeat identical reads and verify reduced database queries
- Trigger entity mutations and verify stale cache entries are evicted
- Inspect hit/miss and eviction counters through admin cache stats API and actuator metrics

Security Verification:
- Ensure unauthenticated users cannot access protected routes
- Ensure non-admin users cannot access admin endpoints
- Verify suspended/banned/deleted user behavior on authentication

Real-Time Verification:
- Confirm WebSocket chat and notification dispatch
- Confirm presence online/offline transitions and inactivity timeout behavior

### 12.3 Suggested Test Evolution
- Service-level unit tests for proposal and notification workflows
- Integration tests for security and authorization boundaries
- Cache behavior tests with repository call count assertions
- End-to-end tests for main user journeys

---

## 13. Deployment and Execution

### 13.1 Local Build
Use Maven wrapper from project root to compile and package the application.

General process:
1. Configure database and environment variables
2. Build the application with Maven wrapper
3. Start the Spring Boot application

### 13.2 Runtime Dependencies
- Java 21 runtime
- MySQL instance reachable by configured datasource URL
- Optional external services for full feature set:
  - Google OAuth2 and Meet APIs
  - Cloudinary

### 13.3 Local Execution Notes
- Server listens on configured port and address
- Static resources and templates are loaded from the custom resource directory configured in Maven build
- Database schema is managed by JPA auto-update setting in development mode

### 13.4 Production Considerations
- Use externalized secrets and secure configuration management
- Restrict debug logging and tighten CORS/origin policies
- Use reverse proxy and TLS termination
- Review session/cookie and security hardening policies

---

## 14. Future Improvements

### 14.1 Scalability and Performance
- Introduce distributed cache (for example Redis) for multi-instance deployments
- Add second-level cache and query-level optimization for heavy analytics use cases
- Add asynchronous processing queues for non-critical background tasks
- Expand database indexing strategy based on production profiling

### 14.2 Observability
- Integrate centralized logging and trace correlation
- Add dashboarded metrics for cache, proposal throughput, notification latency, and WebSocket session counts
- Extend health checks to include external dependency readiness

### 14.3 Security Hardening
- Add rate limiting for sensitive endpoints
- Add account lockout and anomaly detection policies
- Strengthen audit and forensic reporting for administrative actions

### 14.4 Product and Domain Enhancements
- Add richer skill taxonomy and recommendation engine
- Add explicit session scheduling and calendar integrations
- Expand moderation automation with ML-assisted content risk scoring
- Add multilingual UX completion and accessibility improvements

### 14.5 Testing Maturity
- Expand integration and contract tests for APIs
- Add performance regression test suites
- Add WebSocket event consistency tests

---

## 15. Conclusion
SkillSwap is implemented as a layered Spring Boot platform combining community-driven skill exchange workflows, real-time collaboration, moderation controls, and operational observability. The architecture balances functional richness with modular separation, enabling continued evolution toward production-grade scalability, governance, and maintainability.
