# Enum Organisation Platform
Product Requirements Document (PRD)  
Version 1.0 – Candidate Mini-Capstone  
Author: Ifeanyi Cyriacus

## 1. Vision
Give small and mid-size organisations a single place to create programs, invite colleagues, and stay within simple, transparent usage limits—without worrying about infrastructure.

## 2. Personas (Core Users)

| Persona | Primary Jobs 
|---------|-------------
| **Organisation Admin** (Primary User, 1 per org) | Create org, manage billing, invite/delete any member, full data access |
| **Manager** (Secondary User, 0-n per org) | Create & update programs, view team |
| **Member** (End User, 0-n per org) | Read programs, stay informed |
| **System** (Background Supporting User) | Send emails, rotate tokens, enforce limits, health pings and metric colection|

## 3. Functional Requirements

### 3.1 Authentication Service

| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As an Org Admin I want to sign-up with email & password so that I can create my organisation. | Email already exists and verified → return 409 EMAIL_IN_USE. Email exists but unverified → overwrite user, resend token, return 201. | IF email verified flag is true THEN reject duplicate signup. IF verified flag is false THEN issue new token. |
| 2 | As any user I want to receive a verification email so that I can activate my account. | Worker down → email stays “queued”, no HTTP error to caller. Token TTL = 24 h. | IF worker later restarts THEN it picks up rows with status=queued and attempts<=3. |
| 3 | As any user I want to log in so that I can obtain an access token. | Login attempted before verification → 403 EMAIL_NOT_VERIFIED. Password wrong 5× in 5 min → 429 RATE_LIMITED with Retry-After. | IF email.verified == false THEN reject regardless of password. IF rate bucket full THEN reject extra attempts. |
| 4 | As any user I want to refresh my access token so that I don’t retype credentials. | Re-use of old refresh token → revoke entire family, return 401 TOKEN_REUSE_DETECTED. | IF token already used THEN rotate & invalidate chain. |
| 5 | As any user I want to log out so that my session ends. | Double logout → 204 idempotent. | IF session already deleted THEN still return 204. |

### 3.2 Organisation Profile

| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As an Admin I want to upsert logo, website, industry, description so that the org looks professional. | Logo file type not png/jpg → 415 UNSUPPORTED_MEDIA. Website fails URL regex → 422 VALIDATION_ERROR. | IF validation fails THEN error.details.field must show exact keys. |
| 2 | As an Admin I want to see a completeness % so that I know what is missing. | Org created 5 min ago with nothing → completeness=0. After adding logo → 25%. | The formula is `populatedFields/totalFields×100`, rounded down. |

**Completeness fields:** logoUrl, website, industry, description (4 total).

### 3.3 RBAC & Team Invite Flow

| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As an Admin I want to invite someone by email so that they can join with a secure link. | Invite expired (24 h) → 400 TOKEN_EXPIRED. Invite already accepted → 400 TOKEN_ALREADY_USED. Accepting invite with existing email → Conflict error. Only Admin can send invites. | IF current_time > invite.created_at + 24 h THEN reject. one email per user. Invite is single use. |
| 2 | As an invited user I want to accept the invite so that I become an active member. | Email already exists in another org → allow, create separate membership. | IF membership row created THEN token row updated `used=true`. |
| 3 | As an Admin I want to remove a member so that they lose access instantly. | Deletion of last Admin → 400 CANNOT_REMOVE_LAST_ADMIN. | IF count(Admin)==1 AND target.role==Admin THEN reject. |

Role hierarchy matrix (rows = caller, columns = allowed target roles):

| Caller \ Target | ADMIN | MANAGER | MEMBER |
|-----------------|-------|---------|--------|
| ADMIN           | CRUD  | CRUD    | CRUD   |
| MANAGER         | –     | –       | R      |
| MEMBER          | –     | –       | R      |

### 3.4 Programs

| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As Admin/Manager I want to create a program (name, start, end, budget, status) so that I can run initiatives. | End date < start date → 422 VALIDATION_ERROR "date mismatch". Org on Free plan with 3 active programs → 403 LIMIT_EXCEEDED. | IF plan=free AND active_programs≥3 THEN block create. |
| 2 | As any member I want to list & filter programs by status or query so that I can find relevant info. | Filter=ACTIVE&query=“summer” → SQL ILIKE %summer% combined. | Result must respect pagination (default 20, max 100). |
| 3 | As Admin/Manager I want to archive a program so that it doesn’t count against limits. | Already archived → archive again is idempotent 200. | IF status==ARCHIVED THEN no DB change, still 200. |

Status enum: PLANNED, ACTIVE, COMPLETED, ARCHIVED.

### 3.5 Plans & Hard Limits

| Plan      | Member Limit  | Program Limit |
|------     |-------------- |---------------|
| Free      | 5             | 3             |
| Pro       |50             | 20            |
| Enterprise| ∞             | ∞             |


| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As an Admin I want to know my current usage so that I can decide to upgrade. | Usage call returns `{members:4,programs:3,percentage:{members:80,programs:100}}`. | IF percentage≥100 THEN block create. |
| 2 | As an Admin I want to downgrade only when within limits so that data is preserved. | Attempt Pro→Free while 6 members → 403 DOWNGRADE_BLOCKED. | IF future_usage>target_plan THEN reject with details. |

### 3.6 Email Outbox (Mock Worker)

| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As System I want to queue emails so that outages don’t lose messages. | HTTP 201 means “queued”, not “sent”. | IF DB insert ok THEN return 201 immediately. |
| 2 | As System worker I want to retry up to 3× with exponential back-off so that transient failures heal. | Row locked with SELECT … FOR UPDATE skip locked. | IF attempts<3 THEN status=RETRY later. IF attempts≥3 THEN status=FAILED. |
| 3 | As user I want consistent status so that I know the email outcome. | /outbox/{id} returns {status: "sent or failed"}. | IF worker marked sent THEN status=SENT. |

### 3.7 Health & Metrics

Endpoints  
`GET /.well-known/health` →

```json 
{
    status: "UP"|"DOWN",
    checks:[
        {name:"db", status:"UP"|"DOWN"},
        {name:"redis", status:"UP"|"DOWN"}
    ]
}
```  
`GET /metrics` → Prometheus text format (Spring Boot Actuator).

| # | User Story | Edge Cases & Expected Behaviour | Acceptance Criteria |
|---|------------|--------------------------------|---------------------|
| 1 | As a DevOps engineer I want to know if DB is down so that I can page the on-call. | DB connection fails → db.status=DOWN → top-level status=DOWN. | IF any critical check==DOWN THEN HTTP 503. |
| 2 | As a developer I want to scrape metrics so that I can graph them. | /metrics returns content-type text/plain version 0.0.4. | Actuator must not require auth (public). |

### 3.8 Standard Error Envelope

All error responses MUST use:

```json
HTTP 4xx/5xx
{
    "error": {
        "code": "UPPER_SNAKE_CASE",
        "message": "Human friendly",
        "details": [ 
            { 
                "field":"member",
                "limit":"5"
            } 
        ],
        "trace_id": "abc123"
    }
}

```

Code examples:  
`VALIDATION_ERROR`, `LIMIT_EXCEEDED`, `TOKEN_EXPIRED`, `INSUFFICIENT_PRIVILEGE`, `RATE_LIMITED`, `INTERNAL_SERVER_ERROR`.

**Trace id** = generated at edge (`X-Trace-Id` header or UUID).

## 4. Non-Functional Requirements
- **Security:** OWASP top-10 mitigations, bcrypt 12·rounds, JWT signature HS256 256-bit secret, CRSF-safe state-changing operations use Authorization header.
- **Performance:** p95 login < 300 ms, p95 list programs < 200 ms (local Docker, 1 k orgs).
- **Testability:** minimum 70% line coverage (JaCoCo), integration tests via `@SpringBootTest`, embed H2 or Testcontainers Postgres.
- **Observability:**
    - Standardized logging: JSON logs, key=trace_id, include userId where available.
    - Prometheus-compatible metrics endpoint.
- **Data Modelling**
    - Entities must be normalized and logically grouped.
    - Clear separation between core entities and metadata.

- **Portability:** single `docker-compose up` boots app + DB + mailhog (dev SMTP).


## 5. Assumptions & Trade-offs (Out of Scope)
### 5.1 Assumptions
- Email verification is simulated via tokens; actual sending is mocked.
- Redis is optional for session storage but recommended.
- H2 in-memory DB is acceptable for development; Postgres via Docker
  preferred for production-like setup.

### 5.2 Trade-offs
- Rate limiting is implemented at the application level unless Redis is
  available.
- Audit trails are not required but may be added as bonus features.
- No frontend UI is expected; focus is purely backend.



## 6. Future (Out of Scope)
- Real SMTP/SMS, SAML SSO, webhooks, audit table, per-seat pricing API, multiple organisations per user, soft-delete restore UI.

## 7. Glossary
- **Org**: organisation row in organisations table.
- **Membership**: join table (user_id, org_id, role).
- **Program**: initiative row linked to org.
- **Token Family**: set of refresh tokens produced during single session (allows rotation).
- **Outbox**: transactional email queue table.
