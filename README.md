# House App

A prototype residential building management application for apartment administrators and residents.

ISO/IEC 27001-inspired controls are implemented for prototype purposes only.

## Status

Stage 1 authentication foundation is implemented:

- React/Vite frontend in `frontend/`
- Java/Spring Boot backend in `backend/`
- SQLite persistence for users and refresh tokens
- JWT access tokens
- refresh token rotation
- BCrypt password hashing
- forced password change support

Stage 2 residents and apartments are implemented:

- apartment data model and administrator apartment API
- resident profile data model linked to resident users
- administrator resident management API and frontend integration
- resident profile API and frontend page
- server-side resident data isolation
- administrator-created residents with temporary passwords and `mustChangePassword=true`
- avatar path field in the data model

Stage 3 announcements and contacts are implemented:

- SQLite/Flyway tables for announcements and building contacts
- administrator announcement CRUD with publish/archive workflow
- resident read-only published announcement API
- administrator contact CRUD with soft deactivate
- resident read-only active contacts API
- frontend integration for admin/resident announcements and contacts
- bilingual UK/EN announcement and contact fields with frontend fallback to Ukrainian

Stage 4 maintenance requests are implemented:

- SQLite/Flyway table for resident maintenance requests
- resident request creation and personal request history
- administrator request list, filters, status updates, priority updates, responses, and internal notes
- server-side resident ownership checks
- frontend integration for resident and administrator maintenance pages

Stage 5 payments are implemented:

- SQLite/Flyway table for prototype payment/accounting records
- resident read-only payment visibility with ownership checks
- administrator payment creation, editing, status changes, cancellation, and filters
- money stored as integer minor units, for example `125000 = 1250.00 UAH`
- frontend integration for resident and administrator payment pages

Stage 6 audit log and security incidents are implemented:

- SQLite/Flyway tables for append-only audit records and security incident records
- administrator read-only audit log API with filters
- administrator security incident management API with create, edit, status update, and soft false-positive close
- audit events for authentication, residents, maintenance, payments, and security incident operations
- frontend integration for administrator audit log and incidents pages

Avatar upload, full localization, and PWA behavior remain for later stages.

## Requirements

- Java 21 JDK
- Maven
- Node.js and npm

Termux needs a real JDK package with `javac`, not only a JRE.

## Directory Structure

```text
house-app-bak/
  frontend/
    public/
    src/
    package.json
    vite.config.js
  backend/
    pom.xml
    src/
    data/
    uploads/
      avatars/
  AGENTS.md
  README.md
  .gitignore
```

## Backend Configuration

Spring Boot does not read `.env` files automatically. Export variables in the shell before starting the backend.

```bash
cd backend
export APP_JWT_SECRET='replace-with-at-least-32-random-bytes'
export APP_CORS_ALLOWED_ORIGINS='http://localhost:5173'
mvn spring-boot:run
```

Optional variables:

```text
APP_DATABASE_PATH=./data/house-app.db
APP_UPLOAD_DIR=./uploads
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
SERVER_ADDRESS=0.0.0.0
SERVER_PORT=8080
```

For development only, the backend has an explicit unsafe JWT fallback and prints a warning when it is used. Do not use the fallback outside local prototype testing.

SQLite database location:

```text
backend/data/house-app.db
```

## Frontend Configuration

Create `frontend/.env` when running against the backend:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

Then run:

```bash
cd frontend
npm install
npm run dev
```

Production build:

```bash
cd frontend
npm run build
```

## Demo Credentials

- Administrator: `admin@house.com` / `Admin123!`
- Resident: `resident@house.com` / `Resident123!`

These credentials are for prototype demonstration only.

The seed data also creates four extra prototype residents with apartments and temporary password `TempResident1!`. They are marked as requiring password replacement on first login. These credentials are prototype-only and must not be reused outside local demonstration.

Stage 3 seed data includes five announcements: three published records visible to residents, one draft visible only to administrators, and one archived record visible only to administrators.

Stage 3 seed data also includes five building contacts: management company, plumber, electrician, security, and emergency service. The seeder is idempotent and does not create duplicates on restart.

Stage 4 seed data includes eight maintenance requests across the demo residents, including `NEW`, `IN_PROGRESS`, `WAITING_RESIDENT`, `RESOLVED`, and `CANCELLED` statuses, plus urgent and common category examples.

Stage 5 seed data includes twelve payment records across demo residents and apartments, with `PENDING`, `PAID`, `OVERDUE`, and `CANCELLED` statuses, several months, and multiple payment types.

Stage 6 seed data includes at least fifteen audit log records across authentication, resident, announcement, contact, maintenance, payment, incident, and system events. It also includes five security incidents covering `OPEN`, `INVESTIGATING`, `RESOLVED`, `FALSE_POSITIVE`, and high/critical severity examples. The seeder is idempotent and does not create duplicates on restart.

## Authentication Architecture

- Access token lifetime: 15 minutes.
- Access token storage: React application memory only.
- Refresh token lifetime: 7 days, or 30 days with “Remember me”.
- Refresh token storage: frontend `localStorage`.
- Refresh token database storage: SHA-256 hash only.
- Refresh tokens rotate on `/api/auth/refresh`.
- Logout revokes the submitted refresh token.
- Password change revokes old refresh tokens and returns a new token pair.

Refresh tokens in `localStorage` are more exposed to XSS than HttpOnly cookies. This is a prototype tradeoff, not a production-perfect design.

## API

Public:

```text
GET  /api/health
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

Authenticated:

```text
GET  /api/auth/me
POST /api/auth/change-password
```

Administrator residents and apartments:

```text
GET    /api/admin/apartments
POST   /api/admin/apartments
GET    /api/admin/apartments/{id}
PUT    /api/admin/apartments/{id}
DELETE /api/admin/apartments/{id}

GET    /api/admin/residents
POST   /api/admin/residents
GET    /api/admin/residents/{id}
PUT    /api/admin/residents/{id}
DELETE /api/admin/residents/{id}
```

`DELETE /api/admin/residents/{id}` performs a soft deactivate of the linked user instead of physically deleting the account.

Resident profile:

```text
GET /api/resident/profile
PUT /api/resident/profile
```

Residents may update only safe profile fields: phone, emergency contact fields, and preferred language. Apartment assignment, notes, role, email, enabled status, and password-change state remain administrator/server controlled.

Administrator announcements and contacts:

```text
GET    /api/admin/announcements
POST   /api/admin/announcements
GET    /api/admin/announcements/{id}
PUT    /api/admin/announcements/{id}
DELETE /api/admin/announcements/{id}
PATCH  /api/admin/announcements/{id}/publish
PATCH  /api/admin/announcements/{id}/archive

GET    /api/admin/contacts
POST   /api/admin/contacts
GET    /api/admin/contacts/{id}
PUT    /api/admin/contacts/{id}
DELETE /api/admin/contacts/{id}
```

`DELETE /api/admin/announcements/{id}` archives the announcement. `DELETE /api/admin/contacts/{id}` performs a soft deactivate.

Resident announcements and contacts:

```text
GET /api/resident/announcements
GET /api/resident/announcements/{id}
GET /api/resident/contacts
```

Residents can read only published, non-expired announcements and active contacts.

Administrator maintenance requests:

```text
GET   /api/admin/maintenance-requests
GET   /api/admin/maintenance-requests/{id}
PATCH /api/admin/maintenance-requests/{id}
```

The admin list supports `status`, `category`, `priority`, and `search` filters. Admin updates may change status, priority, `adminResponse`, and `internalNotes`.

Resident maintenance requests:

```text
GET  /api/resident/maintenance-requests
POST /api/resident/maintenance-requests
GET  /api/resident/maintenance-requests/{id}
```

Residents can create and read only their own requests. Apartment and resident ownership are derived from the authenticated account, not from request body fields.

Administrator payments:

```text
GET    /api/admin/payments
POST   /api/admin/payments
GET    /api/admin/payments/{id}
PUT    /api/admin/payments/{id}
PATCH  /api/admin/payments/{id}/status
DELETE /api/admin/payments/{id}
```

The admin list supports `status`, `type`, `residentId`, `apartmentId`, `periodYear`, `periodMonth`, and `search` filters. `DELETE` marks a payment as `CANCELLED`; it does not hard delete records.

Resident payments:

```text
GET /api/resident/payments
GET /api/resident/payments/{id}
```

Residents can read only their own payment records. Payment records are prototype accounting records only; no real payment processing is integrated.

Administrator audit logs:

```text
GET /api/admin/audit-logs
GET /api/admin/audit-logs/{id}
```

The audit list supports `action`, `entityType`, `actorUserId`, `entityId`, `dateFrom`, `dateTo`, and `search` filters. Audit logs are append-only through the public API: there are no public create, update, or delete audit log endpoints.

Administrator security incidents:

```text
GET    /api/admin/security-incidents
POST   /api/admin/security-incidents
GET    /api/admin/security-incidents/{id}
PUT    /api/admin/security-incidents/{id}
PATCH  /api/admin/security-incidents/{id}/status
DELETE /api/admin/security-incidents/{id}
```

The incident list supports `severity`, `status`, `category`, `assignedToUserId`, `dateFrom`, `dateTo`, and `search` filters. `DELETE` is a soft close that marks the incident as `FALSE_POSITIVE` and sets `resolvedAt`; it does not hard delete the record.

Role-check endpoints used by Stage 1 tests:

```text
GET /api/admin/auth-check
GET /api/resident/auth-check
```

## Local Run

Backend:

```bash
cd backend
export APP_JWT_SECRET='replace-with-at-least-32-random-bytes'
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
printf 'VITE_API_BASE_URL=http://localhost:8080/api\n' > .env
npm install
npm run dev
```

## Ngrok

Example with separate frontend and backend public URLs:

```bash
cd backend
export APP_JWT_SECRET='replace-with-at-least-32-random-bytes'
export APP_CORS_ALLOWED_ORIGINS='http://localhost:5173,https://your-frontend.ngrok-free.app'
export SERVER_ADDRESS=0.0.0.0
mvn spring-boot:run
```

Set the frontend API URL locally:

```text
VITE_API_BASE_URL=https://your-backend.ngrok-free.app/api
```

Do not commit temporary ngrok URLs. Free ngrok URLs may change between sessions.

## Tests

Backend:

```bash
cd backend
mvn clean test
mvn clean package
```

Frontend:

```bash
cd frontend
npm install
npm run build
```

## Known Limitations

- This is not production-certified.
- No real payment provider is integrated.
- Avatar upload is not implemented yet; Stage 2 stores only `avatarPath`.
- No production SIEM, monitoring pipeline, or incident automation is integrated.
- Full Ukrainian/English localization is not complete; Stage 6 adds only audit and incident UI labels on top of earlier stage labels.
- PWA behavior is not implemented yet.
