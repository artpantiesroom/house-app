# House App

A prototype full-stack residential building management application for apartment administrators and residents.

ISO/IEC 27001-inspired controls are implemented for prototype purposes only.

## Status

The prototype has completed staged implementation through Stage 7B cleanup:

- Stage 1: authentication, JWT access tokens, refresh token rotation, logout, forced password change, role guards.
- Stage 2: residents, apartments, administrator resident CRUD, resident profile, resident data isolation.
- Stage 3: announcements and building contacts for administrator and resident workflows.
- Stage 4: maintenance requests for residents and administrators.
- Stage 5: prototype payment/accounting records.
- Stage 6: backend-created audit logs and administrator security incidents.
- Stage 7A: authenticated avatar upload, replacement, removal, and local avatar serving.
- Stage 7B: frontend mock cleanup, route/navigation review, i18n polish, README finalization, and PWA-readiness review.

This is not production-certified software and is not connected to a real payment provider.

## Stack

Frontend:

- React 18
- Vite
- React Router v6
- Tailwind CSS
- Framer Motion
- Lucide React
- React Context API
- native `fetch`

Backend:

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- SQLite
- Flyway
- JWT
- BCrypt
- JUnit 5 / Spring Boot Test

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

## Requirements

- Java 21 JDK
- Maven
- Node.js and npm

Termux should use a JDK package that includes `javac`, not only a JRE.

## Configuration

Spring Boot does not read `.env` files automatically. Export variables in the shell before starting the backend.

Backend variables:

```text
APP_JWT_SECRET=replace-with-at-least-32-random-bytes
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_DATABASE_PATH=./data/house-app.db
APP_UPLOAD_DIR=./uploads
APP_JWT_ACCESS_TOKEN_SECONDS=900
APP_REFRESH_TOKEN_DAYS=7
APP_REFRESH_TOKEN_REMEMBER_DAYS=30
SERVER_ADDRESS=0.0.0.0
SERVER_PORT=8080
```

Frontend variable:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

Example files are provided at:

- `backend/.env.example`
- `frontend/.env.example`

Do not commit active `.env` files or real secrets.

## Local Run

Backend:

```bash
cd backend
export APP_JWT_SECRET='replace-with-at-least-32-random-bytes'
export APP_CORS_ALLOWED_ORIGINS='http://localhost:5173'
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Production frontend build:

```bash
cd frontend
npm run build
```

## Demo Accounts

- Administrator: `admin@house.com` / `Admin123!`
- Resident: `resident@house.com` / `Resident123!`

These credentials and seeded records are for local prototype demonstration only. Additional seeded resident accounts may require password replacement on first login.

## Implemented Modules

Authentication:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/change-password`
- `GET /api/auth/me`

Residents and apartments:

- Administrator resident management under `/api/admin/residents`
- Administrator apartment management under `/api/admin/apartments`
- Resident profile under `/api/resident/profile`
- Server-side resident ownership checks

Announcements and contacts:

- Administrator announcement CRUD and publish/archive workflow
- Resident read-only published announcements
- Administrator contact CRUD with soft deactivate
- Resident read-only active contacts

Maintenance:

- Resident request creation and personal request history
- Administrator request filters and status/priority/response updates

Payments:

- Administrator payment creation, editing, status changes, and cancellation
- Resident read-only access to their own payment records
- Amounts are stored as integer minor currency units, for example `10000 = 100.00 UAH`
- No real payment processing is integrated

Audit and incidents:

- Backend-created audit records
- Administrator read-only audit log access
- Administrator security incident management
- Audit records are append-only through the public API: there are no public create, update, or delete audit endpoints

Avatars:

- Resident avatar upload, replacement, and removal
- Administrator avatar upload, replacement, and removal for existing resident profiles
- Authenticated avatar file serving through `/api/files/avatars/{filename}`
- Allowed formats: JPEG, PNG, WebP
- Maximum file size: 2 MB

## Storage

SQLite database:

```text
backend/data/house-app.db
```

Avatar uploads:

```text
backend/uploads/avatars/
```

Only generated avatar filenames/relative paths are stored in SQLite. API responses may include `avatarUrl`; absolute filesystem paths are not exposed.

## Security Notes

- JWT access tokens are short-lived and stored only in React application memory.
- Refresh tokens are stored in frontend `localStorage` and only SHA-256 hashes are stored in SQLite.
- Refresh tokens rotate on refresh and are revoked on logout.
- Passwords are hashed with BCrypt.
- Role-based access is enforced server-side for administrator and resident endpoints.
- New administrator-created residents receive temporary passwords and must replace them when required.
- Refresh tokens in `localStorage` are more exposed to XSS than HttpOnly cookies. This is a prototype tradeoff, not a production-perfect design.
- CORS allowed origins are configured through `APP_CORS_ALLOWED_ORIGINS`.
- No password hashes, refresh token hashes, access tokens, or raw passwords should be exposed through APIs or logs.

## Routes

Frontend routes include:

```text
/login
/change-password
/forbidden
/admin/dashboard
/admin/residents
/admin/announcements
/admin/maintenance
/admin/payments
/admin/audit-log
/admin/incidents
/admin/contacts
/resident/home
/resident/requests
/resident/payments
/resident/contacts
/resident/profile
```

Unknown frontend routes redirect to `/login`; authenticated users are then routed by role through the existing guards.

## Tunnel Usage

For ngrok or a similar tunnel, expose frontend and backend separately if needed:

```bash
cd backend
export APP_JWT_SECRET='replace-with-at-least-32-random-bytes'
export APP_CORS_ALLOWED_ORIGINS='http://localhost:5173,https://your-frontend.ngrok-free.app'
export SERVER_ADDRESS=0.0.0.0
mvn spring-boot:run
```

Set the frontend API URL:

```text
VITE_API_BASE_URL=https://your-backend.ngrok-free.app/api
```

Do not commit temporary ngrok URLs. Free ngrok URLs may change between sessions.

## PWA Readiness / Future Work

The frontend is structured for future PWA work through a centralized API client, configurable backend URL, responsive layouts, role guards, and centralized authentication state.

Full PWA behavior is not implemented. There is no service worker, offline cache, push notification flow, background sync, or install prompt.

## Testing

Backend:

```bash
cd backend
mvn clean test
mvn clean package
```

Frontend:

```bash
cd frontend
npm run build
```

Frontend test infrastructure is not currently included.

## Known Limitations

- Prototype only; not production-certified.
- No real payment gateway or payment processing.
- No cloud/object storage for avatar files.
- No production SIEM, monitoring pipeline, or automated incident response.
- No full PWA/offline mode.
- No avatar image resizing, cropping, optimization, or malware scanning.
- No email/SMS notification workflow.
- Building information shown on resident home/contact pages is currently a small static frontend configuration because no dedicated backend building-info endpoint exists.
