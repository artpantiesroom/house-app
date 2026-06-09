# AGENTS.md

## Project Overview

This project is a test but functional residential building management application.

The project originally existed as a frontend-only React prototype. It is now being converted into a full-stack application with a Java backend and SQLite database.

The application is intended for two user roles:

1. Administrator
2. Resident

The administrator manages residents, announcements, maintenance requests, payments, contacts, incidents, and audit information.

The resident can access only their own data and public building information.

This is still a prototype project. It is not production-certified, is not connected to a real payment provider, and must not claim full ISO/IEC 27001 compliance.

Use this wording where appropriate:

```text
ISO/IEC 27001-inspired controls are implemented for prototype purposes only.
```

---

## Main Goal

Convert the existing frontend prototype into a clean, functional, full-stack application.

The application must:

* preserve the existing blue animated interface;
* preserve the existing roles and routes where practical;
* replace mock backend behavior with a real Java REST backend;
* use SQLite for persistent storage;
* support secure login with JWT;
* support administrator-created resident accounts;
* require password replacement after the first login;
* connect the existing React frontend to the backend;
* remain lightweight enough to run on Termux;
* remain prepared for future PWA integration;
* be accessible through an external tunnel such as ngrok;
* be implemented gradually, one functional stage at a time.

Do not attempt to implement the entire application in a single task unless explicitly requested.

---

## Repository Structure

The final repository must use this structure:

```text
house-app-bak/
  frontend/
    src/
    public/
    package.json
    vite.config.js

  backend/
    pom.xml
    src/
      main/
        java/
        resources/
      test/
        java/
    data/
    uploads/
      avatars/

  AGENTS.md
  README.md
  .gitignore
```

The existing React frontend must be moved into:

```text
frontend/
```

The Java backend must be created in:

```text
backend/
```

Do not modify the separate `house-app` directory.

The `house-app` directory is the preserved frontend-only version.

All full-stack work must be performed only inside:

```text
house-app-bak
```

---

## Frontend Tech Stack

Use:

* React 18
* Vite
* React Router v6
* Tailwind CSS
* Framer Motion
* Lucide React
* React Context API
* native `fetch`
* browser `localStorage` only where explicitly allowed
* browser `sessionStorage` only where explicitly allowed

Do not introduce:

* Next.js
* Angular
* Vue
* Redux
* Zustand
* MobX
* React Query
* SWR
* Axios
* Firebase
* Supabase
* GraphQL
* Bootstrap
* Material UI
* Ant Design
* Chakra UI
* DaisyUI

Do not replace the current frontend framework or visual design.

---

## Backend Tech Stack

Use:

* Java 21
* Spring Boot 3
* Maven
* Spring Web
* Spring Security
* Spring Data JPA
* Bean Validation
* SQLite
* Flyway
* JWT
* BCrypt
* JUnit 5
* Mockito where useful
* Spring Boot Test

Do not use:

* Gradle
* PostgreSQL
* MySQL
* MariaDB
* MongoDB
* Redis
* Kafka
* Docker as a required runtime dependency
* external authentication providers
* real payment gateways
* GraphQL
* Swagger or OpenAPI UI unless explicitly requested later

The backend must remain runnable on Termux.

Avoid unnecessary libraries and resource-heavy infrastructure.

---

## Build and Run Requirements

The frontend must remain runnable with:

```bash
cd frontend
npm install
npm run dev
```

The backend must remain runnable with:

```bash
cd backend
mvn spring-boot:run
```

The backend build must work with:

```bash
cd backend
mvn clean test
mvn clean package
```

The frontend production build must work with:

```bash
cd frontend
npm run build
```

Do not mark a stage as complete unless the affected commands work.

---

## Development Process

Implement the project in stages.

Required order:

### Stage 1 — Authentication foundation

Implement:

* backend project structure;
* SQLite connection;
* Flyway migrations;
* User entity;
* Role enum;
* RefreshToken entity;
* JWT access token;
* refresh token;
* login endpoint;
* token refresh endpoint;
* logout endpoint;
* BCrypt password hashing;
* forced password change after first login;
* initial demo accounts;
* frontend login integration;
* frontend token refresh;
* protected frontend routes;
* role-based frontend navigation.

Do not migrate unrelated mock modules during Stage 1.

### Stage 2 — Residents and apartments

Implement:

* resident management;
* apartment association;
* administrator CRUD operations;
* resident profile;
* resident data isolation;
* avatar path support if needed by the data model.

### Stage 3 — Announcements and contacts

Implement:

* public and resident-visible announcements;
* administrator announcement CRUD;
* building contacts;
* administrator contact CRUD.

### Stage 4 — Maintenance requests

Implement:

* resident request creation;
* resident request history;
* administrator request management;
* request statuses;
* audit logging of important changes.

### Stage 5 — Payments

Implement:

* payment records;
* administrator payment management;
* resident access only to their own payments;
* payment statuses;
* amounts stored as integer minor currency units.

### Stage 6 — Audit log and security incidents

Implement:

* persistent backend audit log;
* security incident records;
* administrator-only access;
* immutable audit records through the public API.

### Stage 7 — Avatar upload, localization, and cleanup

Implement:

* optional resident avatar upload;
* Ukrainian and English interface localization;
* removal of obsolete frontend mock logic;
* final integration cleanup;
* PWA-readiness review.

Do not skip stages without explicit permission.

At the beginning of each stage:

1. inspect the current project;
2. explain the plan;
3. list files that will be created or changed;
4. wait for confirmation if the requested task explicitly asks for planning only.

At the end of each stage:

1. run relevant tests;
2. run relevant builds;
3. list changed files;
4. explain completed behavior;
5. report known limitations honestly;
6. do not claim success if commands fail.

---

## Authentication Architecture

Use JWT authentication.

### Access token

* Short-lived JWT.
* Default lifetime: 15 minutes.
* Stored only in React application memory.
* Must not be stored in `localStorage`.
* Sent using:

```http
Authorization: Bearer <access-token>
```

### Refresh token

* Stored in frontend `localStorage`.
* Default lifetime: 7 days.
* Lifetime with “Remember me”: 30 days.
* Store only a cryptographic hash of the refresh token in SQLite.
* Rotate the refresh token when it is used.
* Revoke it on logout.
* Reject expired or revoked tokens.
* A user may have multiple active sessions only if the implementation explicitly supports them.

Because this is a prototype and refresh tokens are stored in `localStorage`, document that this approach is more exposed to XSS than an HttpOnly cookie.

Do not describe this authentication design as production-perfect.

### Authentication endpoints

Use routes similar to:

```text
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/change-password
GET  /api/auth/me
```

Request and response DTOs must be separate from JPA entities.

Never return password hashes or refresh token hashes.

---

## Initial Accounts

Seed these accounts for initial demonstration:

```text
admin@house.com / Admin123!
resident@house.com / Resident123!
```

The administrator account has role:

```text
ADMIN
```

The resident account has role:

```text
RESIDENT
```

These accounts are for prototype demonstration only.

The seeded resident account may be marked as already initialized unless a task explicitly requires demonstrating first-login password replacement.

All new residents created by the administrator must receive a temporary password and must have:

```text
mustChangePassword = true
```

---

## First Login Password Change

When the administrator creates a resident account:

1. the administrator provides or generates a temporary password;
2. the backend hashes the temporary password with BCrypt;
3. the raw password is never stored;
4. `mustChangePassword` is set to `true`;
5. the resident can authenticate using the temporary password;
6. after login, the resident must be redirected to the password-change page;
7. access to ordinary resident functions must remain blocked until password replacement;
8. after successful replacement, set `mustChangePassword` to `false`;
9. revoke old refresh tokens after password replacement;
10. issue a new access and refresh token pair if appropriate.

The backend must enforce this restriction.

Frontend-only protection is not sufficient.

---

## Authorization Rules

Use strict server-side authorization.

### Administrator

The administrator may access administrative endpoints and routes.

Examples:

```text
/api/admin/**
/admin/**
```

### Resident

The resident may access resident endpoints and routes.

Examples:

```text
/api/resident/**
/resident/**
```

A resident must only be able to read or modify records belonging to that resident, unless the record is public.

Never trust a resident ID received from the frontend without checking the authenticated user.

### Unauthenticated users

Unauthenticated API requests must return an appropriate HTTP status, normally:

```text
401 Unauthorized
```

### Unauthorized users

Authenticated users without permission must receive:

```text
403 Forbidden
```

Frontend routes must also redirect appropriately, but frontend route guards do not replace backend authorization.

---

## SQLite Rules

Use SQLite as the persistent database.

Database location:

```text
backend/data/house-app.db
```

The backend must create required directories when practical.

Use Flyway for schema migrations.

Do not rely on Hibernate auto-creating production schema.

Prefer:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

after migrations are established.

Use SQLite-compatible SQL.

Do not write migrations containing PostgreSQL-only or MySQL-only syntax.

Configure database access conservatively because the application may run on low-resource Termux hardware.

Do not design the application around high write concurrency.

---

## Data Seeding

Provide idempotent prototype data seeding.

At minimum, initial data should include:

* one administrator account;
* one resident account;
* sample building information;
* sample announcements;
* sample maintenance requests;
* sample payment records;
* sample contacts;
* sample security incidents;
* initial audit records where appropriate.

Seeding must not create duplicate data on every restart.

Passwords must be BCrypt hashes.

Never store seeded raw passwords in the database.

---

## Payment Rules

Payments are prototype accounting records only.

Do not integrate a real payment provider.

A payment record should support fields such as:

* id;
* resident;
* apartment;
* billing period;
* amount;
* status;
* due date;
* paid date;
* description;
* created timestamp;
* updated timestamp.

Allowed statuses:

```text
PENDING
PAID
OVERDUE
```

Store monetary amounts as integer minor currency units.

For Ukrainian hryvnia:

```text
10000 = 100.00 UAH
```

Do not use `double` or `float` for money.

The administrator may create and update payment records.

A resident may only view their own payment records.

---

## Avatar Upload Rules

Residents may optionally upload an avatar.

Store avatar files in:

```text
backend/uploads/avatars/
```

Store only a relative path or generated filename in SQLite.

Allowed formats:

* JPEG
* PNG
* WebP

Maximum file size:

```text
2 MB
```

Requirements:

* validate MIME type;
* validate file extension;
* generate a server-side filename;
* prevent path traversal;
* do not trust the original filename;
* allow one active avatar per resident;
* delete or replace the previous avatar safely;
* provide a default avatar when none exists.

Do not store avatar binary data directly in SQLite.

Do not implement other file uploads unless explicitly requested.

---

## Audit Logging Rules

Audit records must be created by the backend.

The frontend must not be able to submit arbitrary audit records.

Audit records must not be editable or deletable through public application endpoints.

The administrator may view audit records.

A resident must not access the full audit log.

Suggested audit fields:

```text
id
timestamp
actorUserId
actorEmail
action
targetType
targetId
result
ipAddress
details
```

Do not record:

* raw passwords;
* access tokens;
* refresh tokens;
* password hashes;
* sensitive authorization headers.

Log important events such as:

* successful login;
* failed login;
* logout;
* token refresh failure;
* password change;
* forbidden access;
* resident creation;
* resident editing;
* resident deactivation;
* announcement creation;
* announcement editing;
* announcement deletion;
* maintenance request creation;
* maintenance status change;
* payment creation;
* payment update;
* avatar replacement.

---

## API Design Rules

Use REST endpoints under:

```text
/api
```

Use DTOs for all external request and response bodies.

Do not expose JPA entities directly.

Use:

* request DTOs;
* response DTOs;
* validation annotations;
* centralized exception handling;
* clear HTTP status codes;
* consistent error responses.

Suggested error response:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/example",
  "fieldErrors": {
    "email": "Invalid email format"
  }
}
```

Do not expose stack traces to the frontend.

Use pagination for lists that may grow, particularly:

* residents;
* audit log;
* maintenance requests;
* payments;
* incidents.

---

## Frontend API Integration

Replace local mock behavior gradually.

Do not delete all mock data at the beginning.

During each stage:

1. connect only the relevant module to the backend;
2. keep unrelated modules working;
3. remove obsolete mock logic only after the real module works;
4. preserve existing route behavior where practical;
5. preserve existing loading, error, and success UI.

Use native `fetch`.

Create a reusable API layer, for example:

```text
frontend/src/api/
  apiClient.js
  authApi.js
  residentsApi.js
  announcementsApi.js
  maintenanceApi.js
  paymentsApi.js
```

The API client must:

* add the access token;
* handle JSON responses;
* handle API errors;
* attempt one token refresh after a 401 when appropriate;
* avoid infinite refresh loops;
* log out when refresh fails;
* support a configurable backend base URL.

Use an environment variable:

```text
VITE_API_BASE_URL
```

Example:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

Do not hardcode a local IP or ngrok URL in source files.

---

## External Tunnel and CORS Rules

The application may be exposed through ngrok or a similar external tunnel.

The frontend and backend may use different public URLs.

CORS configuration must be controlled through configuration or environment variables.

Use a setting similar to:

```text
APP_CORS_ALLOWED_ORIGINS
```

Examples may include:

```text
http://localhost:5173
https://example-frontend.ngrok-free.app
```

Do not use unrestricted wildcard CORS with credentials or authorization-sensitive endpoints.

Do not hardcode a temporary ngrok domain into committed Java or JavaScript code.

The backend must bind to:

```text
0.0.0.0
```

when external access is required.

Document that free ngrok URLs may change between sessions.

Since JWT is sent in the Authorization header and no authentication cookie is used, configure CORS to allow the `Authorization` and `Content-Type` headers.

---

## Localization Rules

The interface must support:

* Ukrainian (`uk`);
* English (`en`).

Ukrainian is the default language.

Persist the selected language in:

```text
localStorage
```

Use a clean translation structure, for example:

```text
frontend/src/i18n/
  index.js
  uk.js
  en.js
```

Do not scatter hardcoded translations across components when avoidable.

Backend API field names, class names, database names, and code identifiers must remain in English.

User-facing backend validation messages may be translated later.

Do not make backend business logic depend on the selected frontend language.

---

## PWA Readiness

Do not implement a full PWA yet.

Do not add a service worker, offline cache, push notifications, or install prompt unless explicitly requested.

Prepare the architecture for future PWA integration:

* use responsive mobile-first layouts;
* avoid assumptions that the app always runs in a desktop browser;
* use configurable API URLs;
* keep authentication logic centralized;
* keep network access centralized;
* do not store critical application state only in component-local state;
* make loading and offline-like errors understandable;
* keep icons and assets organized;
* avoid browser APIs without graceful fallback.

Do not claim that the application is already a PWA.

---

## Frontend Routes

Preserve or adapt the current routes where practical.

Expected routes include:

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

All routes must render meaningful UI.

All navigation links must point to valid routes.

Role guards must work after page refresh.

---

## Frontend Styling Rules

Preserve the current visual style.

Use the existing blue/ocean palette:

```text
primary: #0EA5E9
accent: #38BDF8
dark background: #0C1A2E
soft white: #F0F9FF
```

Design characteristics:

* blue/ocean theme;
* glassmorphism;
* backdrop blur;
* rounded cards;
* subtle shadows;
* smooth animations;
* responsive layouts;
* mobile-first navigation;
* desktop sidebar;
* Framer Motion transitions;
* Lucide React icons;
* visible loading and error states.

Do not replace the design system unless explicitly requested.

---

## Input Validation and Security

Validate inputs on both frontend and backend.

Backend validation is authoritative.

Validate:

* required values;
* email format;
* password strength;
* text length;
* enum values;
* file size;
* file type;
* numeric ranges;
* dates;
* ownership and authorization.

Do not rely only on stripping characters.

Use React escaping and backend validation rather than custom ad-hoc HTML sanitization.

Do not store or log raw passwords.

Do not return internal exception messages.

Do not expose SQLite files or upload directories as unrestricted static directories.

Add sensible security headers where practical.

Do not claim the prototype is fully secure or certified.

---

## Password Rules

Passwords must:

* be hashed with BCrypt;
* never be stored as plain text;
* never be logged;
* never be returned from APIs;
* never be inserted into audit details.

Use reasonable prototype validation:

* minimum 8 characters;
* at least one uppercase letter;
* at least one lowercase letter;
* at least one digit;
* at least one special character.

Temporary passwords must follow the same requirements.

---

## Session and Logout Behavior

Frontend behavior:

* keep access token in memory;
* keep refresh token in `localStorage`;
* restore authentication by calling the refresh endpoint;
* clear all tokens on logout;
* redirect to login after refresh failure;
* do not display protected content before session restoration completes.

Backend behavior:

* validate JWT signature and expiry;
* revoke refresh token on logout;
* rotate refresh tokens;
* invalidate relevant refresh tokens after password change;
* reject disabled users;
* enforce `mustChangePassword`.

---

## Data Classification

Continue using data classification labels where relevant.

Classifications:

* Confidential
* Internal
* Public

Suggested usage:

* Confidential: payment amounts, apartment identifiers, resident contact details;
* Internal: maintenance details, resident names, administrative notes;
* Public: public announcements and building contacts.

These labels are informational prototype UI controls.

They do not represent formal certification.

---

## Accessibility Rules

Use semantic HTML where practical.

Requirements:

* buttons must use `<button>`;
* inputs must have labels;
* focus states must remain visible;
* status must not rely only on color;
* forms must show accessible validation messages;
* modal dialogs must be keyboard-accessible;
* navigation must be usable on mobile;
* language selector must have an accessible label.

---

## Code Style

### Frontend

Use:

* functional components;
* hooks;
* small reusable components;
* descriptive names;
* centralized API functions;
* centralized authentication handling;
* clear loading and error states.

### Backend

Use clear layers:

```text
controller
service
repository
entity
dto
security
config
exception
mapper
```

Prefer constructor injection.

Do not use field injection.

Keep controllers thin.

Place business logic in services.

Do not return entities directly from controllers.

Use transactions where a multi-step database operation must remain atomic.

Prefer readable code over clever code.

---

## Hard Rules

Never leave:

* TODO comments;
* pseudocode;
* empty methods;
* placeholder pages;
* broken imports;
* broken routes;
* missing referenced files;
* disabled tests used to hide failures;
* hardcoded temporary ngrok URLs;
* hardcoded JWT secrets;
* plain-text passwords;
* exposed password hashes;
* exposed refresh token hashes;
* unrestricted administrator endpoints;
* resident access to another resident’s data.

Do not silently remove existing functionality.

Do not rewrite the entire frontend unnecessarily.

Do not implement later stages while an earlier stage is still broken.

Do not state that work is complete unless it has been tested.

---

## Configuration and Secrets

Use environment variables or external configuration for:

* JWT secret;
* JWT access token lifetime if configurable;
* refresh token lifetime if configurable;
* frontend public URL;
* backend public URL;
* allowed CORS origins;
* database path where useful;
* upload directory where useful.

Provide safe development defaults only when appropriate.

Never commit real secrets.

Provide an example configuration file, such as:

```text
backend/.env.example
frontend/.env.example
```

Do not commit active `.env` files.

For Spring Boot, document how environment variables map into `application.properties`.

---

## Git Ignore Rules

The root `.gitignore` must include at least:

```gitignore
# Frontend
frontend/node_modules/
frontend/dist/
frontend/.env
frontend/.env.local

# Backend
backend/target/
backend/.env
backend/data/*.db
backend/data/*.db-shm
backend/data/*.db-wal

# Uploaded files
backend/uploads/avatars/*
!backend/uploads/avatars/.gitkeep

# IDE
.idea/
.vscode/
*.iml

# OS
.DS_Store
Thumbs.db
```

Do not remove existing useful ignore rules.

---

## README Rules

The root `README.md` must eventually contain:

* project name;
* project status;
* prototype disclaimer;
* frontend stack;
* backend stack;
* directory structure;
* Arch Linux requirements;
* Termux requirements;
* frontend installation;
* backend installation;
* run commands;
* environment configuration;
* demo credentials;
* ngrok or tunnel usage;
* security limitations;
* PWA-readiness note;
* database location;
* avatar upload location;
* testing commands.

Do not continue describing the project as frontend-only after the backend is integrated.

---

## Before Finishing Any Task

Verify all items relevant to the current stage.

### General

* no broken imports;
* no missing files;
* no TODO comments;
* no accidental changes to `house-app`;
* changes are limited to `house-app-bak`;
* documentation reflects actual behavior.

### Frontend

* `npm install` succeeds;
* `npm run dev` starts;
* `npm run build` succeeds;
* routes render;
* navigation links work;
* loading states work;
* API errors are visible;
* Ukrainian is the default language;
* language selection persists;
* mobile layout remains usable.

### Backend

* `mvn clean test` succeeds;
* `mvn clean package` succeeds;
* SQLite migrations apply;
* application starts;
* no raw passwords are stored;
* entities are not exposed directly;
* validation works;
* authorization works;
* resident data isolation works;
* secrets are not hardcoded.

### Authentication

* administrator login works;
* resident login works;
* invalid credentials fail safely;
* access token expires correctly;
* refresh works;
* refresh token rotation works;
* logout revokes refresh token;
* forced password change works;
* ordinary routes remain blocked until password change;
* admin and resident permissions are separated;
* authentication restores after page reload.

### Integration

* frontend uses `VITE_API_BASE_URL`;
* CORS is configurable;
* local development works;
* external tunnel configuration is documented;
* no temporary ngrok URL is committed.

When a check cannot be completed, state exactly what was not verified and why.

