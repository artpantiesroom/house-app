# House App

A prototype residential building management application for apartment administrators and residents.

The repository is prepared as a monorepo. The current implemented application is the React/Vite frontend in `frontend/`. The `backend/` directory is reserved for the future Java backend and is intentionally empty for now.

ISO/IEC 27001-inspired controls are implemented for prototype purposes only.

## Current Frontend Stack

- React 18
- Vite
- React Router v6
- Tailwind CSS
- Framer Motion
- Lucide React
- uuid
- React Context API

## Run Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

The frontend production build should run with:

```bash
cd frontend
npm run build
```

## Demo Credentials

- Administrator: `admin@house.com` / `Admin123!`
- Resident: `resident@house.com` / `Resident123!`

These credentials are for prototype demonstration only.

## Directory Structure

```text
house-app-bak/
  frontend/
    public/
    src/
    package.json
    vite.config.js
  backend/
  AGENTS.md
  README.md
  .gitignore
```

## Prototype Notes

The current implemented app still uses mock data and simulated server-like behavior in the frontend. It does not yet use a real backend, database, real authentication provider, or real payment system.

The project is not certified and is not production-secure.
