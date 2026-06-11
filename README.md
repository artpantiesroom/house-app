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

Announcements, maintenance, payments, contacts, audit log, and incidents still use frontend mock data until later stages.

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
- Announcements, maintenance, payments, contacts, full audit log, and incidents still use mock data or Stage 1 placeholders.
- Full Ukrainian/English localization is planned for a later stage.
