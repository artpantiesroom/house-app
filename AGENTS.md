# AGENTS.md



## Project Overview



This project is a frontend-only prototype of a residential building management application.



The app is for two types of users:



1. Administrator

2. Resident



The administrator has full access to the system.



The resident has limited access only to their own data and public building information.



This is a test/prototype project. There is no real backend, database, API, or production authentication.



## Tech Stack



Use:



* React 18

* Vite

* React Router v6

* Tailwind CSS

* Framer Motion

* Lucide React

* uuid

* React Context API



Do not introduce a different framework or state manager unless explicitly requested.



Do not use:



* fetch

* axios

* real backend

* REST API

* GraphQL

* Firebase

* Supabase

* external database

* real payment systems

* real authentication providers



## Main Goal



Build and maintain a clean, runnable, mobile-first residential building management prototype.



The app must include:



* login

* two roles: Administrator and Resident

* role-based access control

* admin dashboard

* resident dashboard

* announcements

* maintenance requests

* payments

* building contacts

* mock audit log

* mock security incidents

* ISO/IEC 27001-style security simulations

* blue animated interface



The project must always remain runnable with:



```bash

npm install

npm run dev

```



## Hard Rules



Never leave:



* TODO comments

* pseudo-code

* placeholder pages

* broken imports

* broken routes

* empty components

* unfinished features



Every route must render meaningful UI.



All files referenced in imports must exist.



All navigation links must point to valid routes.



## Mock Async Rules



All server-like behavior must be simulated locally.



Use local mock arrays and Promise + setTimeout.



Example:



```js

const simulateRequest = (data, delay = 800) =>

  new Promise((resolve) => setTimeout(() => resolve(data), delay));

```



Required simulated delays:



* login: 1200ms

* page data loading: 600-900ms

* form submit: 1000ms

* CRUD operation: 800ms

* payment fetch: 700ms



Do not use real network requests.



## Authentication Rules



Demo credentials:



```txt

admin@house.com / Admin123!

resident@house.com / Resident123!

```



Authentication behavior:



* Admin redirects to `/admin/dashboard`

* Resident redirects to `/resident/home`

* Invalid credentials show an error

* Login shows loading spinner

* Login simulates password hashing message

* Raw passwords must never be stored



Session behavior:



* Use sessionStorage by default

* Use localStorage only if "Remember session" is enabled

* localStorage session must have a mock expiry timestamp

* Restore session on app load

* Store only safe metadata:



  * id

  * name

  * email

  * role

  * lastLoginTime



## Authorization Rules



Use strict role-based access control.



Admin:



* can access `/admin/*`



Resident:



* can access `/resident/*`

* cannot access `/admin/*`



Unauthenticated users:



* redirect to `/login`



Unauthorized users:



* redirect to `/forbidden`



A resident trying to open an admin route must create a denied audit log entry.



## File Structure



Keep this structure:



```txt

src/

  App.jsx

  main.jsx

  index.css

  components/

    ProtectedRoute.jsx

    SessionTimeoutModal.jsx

    AuditLogTable.jsx

    DataClassificationBadge.jsx

    PasswordStrengthIndicator.jsx

    LoadingSpinner.jsx

    SkeletonCard.jsx

    Sidebar.jsx

    StatusBadge.jsx

    FooterSecurityBadge.jsx

  pages/

    Login.jsx

    Forbidden.jsx

    admin/

      Dashboard.jsx

      Residents.jsx

      Announcements.jsx

      MaintenanceAdmin.jsx

      Payments.jsx

      AuditLog.jsx

      Incidents.jsx

      Contacts.jsx

    resident/

      Home.jsx

      MyRequests.jsx

      MyPayments.jsx

      Contacts.jsx

  context/

    AuthContext.jsx

    DataContext.jsx

    AuditContext.jsx

  data/

    mockData.js

  layouts/

    AdminLayout.jsx

    ResidentLayout.jsx

```



Put reusable UI components in:



```txt

src/components/

```



Put admin pages in:



```txt

src/pages/admin/

```



Put resident pages in:



```txt

src/pages/resident/

```



Put shared mock data in:



```txt

src/data/mockData.js

```



## Styling Rules



Use Tailwind CSS.



Do not add:



* Bootstrap

* Material UI

* Ant Design

* Chakra UI

* DaisyUI



Use this blue/ocean palette:



```txt

primary: #0EA5E9

accent: #38BDF8

dark background: #0C1A2E

soft white: #F0F9FF

```



Design style:



* blue/ocean theme

* glassmorphism cards

* backdrop blur

* rounded corners

* subtle shadows

* responsive layout

* mobile-first design

* smooth page transitions

* Framer Motion animations

* hover scale effects

* Lucide React icons



## Layout Rules



Create separate layouts:



* AdminLayout

* ResidentLayout



Each layout must show:



* navigation

* current user email

* current user role

* last login time

* logout button

* security footer badge



Desktop:



* use sidebar navigation



Mobile:



* use bottom navigation or collapsible menu



## ISO/IEC 27001 Simulation Rules



This project simulates security controls inspired by ISO/IEC 27001.



Do not claim that the app is certified or production-secure.



Use this wording where needed:



```txt

ISO/IEC 27001-inspired controls are simulated for prototype purposes only.

```



Required simulated controls:



### Access Control



* Role-based access control

* Protected routes

* Separate admin and resident areas

* Forbidden page

* Audit denied access attempts



### Session Management



* sessionStorage default session

* localStorage only for Remember session

* mock expiry timestamp

* inactivity timeout

* warning modal before logout

* logout after timeout



### Audit Logging



Use AuditContext.



Audit log entry format:



```js

{

  id: "uuid",

  timestamp: "ISO8601 string",

  actor: "admin@house.com",

  action: "LOGIN",

  target: "Auth",

  result: "SUCCESS"

}

```



Log:



* successful login

* failed login

* logout

* forbidden route access

* maintenance request created

* maintenance status changed

* resident created

* resident edited

* resident deleted

* announcement created

* announcement edited

* announcement deleted



Persist audit log changes in sessionStorage.



### Data Classification



Create and use DataClassificationBadge.



Badges:



* Confidential

* Internal

* Public



Use:



* Confidential for payment amounts, apartment numbers, resident contact info

* Internal for maintenance request details, resident names, admin notes

* Public for announcements and public contacts



### Cryptography Simulation



On login submit, show:



```txt

Password hashed with bcrypt (simulation)

```



Do not actually hash passwords.



Do not store raw passwords.



Show footer badge:



```txt

Connection secured · TLS 1.3 (simulated)

```



### Input Validation



All forms must validate:



* required fields

* email format where applicable

* password strength where applicable

* max text length

* safe text input



Strip these characters from text input:



```txt

< > "

```



Show inline errors.



### Incident Response Simulation



Include mock incidents.



Each incident must have:



* title

* severity

* timestamp

* recommended action

* status



Severity values:



* Low

* Medium

* High



## Data Rules



All mock data must be in:



```txt

src/data/mockData.js

```



Include at minimum:



* 5 residents

* 3 announcements

* 4 maintenance requests

* payment records for 2 months per resident

* 3 security incidents

* 5 audit log entries

* building contacts

* building information



## Context Rules



Use React Context API only.



Required contexts:



* AuthContext

* DataContext

* AuditContext



Do not add Redux, Zustand, MobX, React Query, or SWR.



## UX Rules



Use:



* loading spinner on login

* skeleton loaders on data pages

* visible success/error messages

* status badges

* data classification badges

* clear empty states

* responsive navigation

* animations that feel smooth but not excessive



## Accessibility Rules



Use semantic HTML where possible.



Buttons must be real button elements.



Inputs must have labels.



Interactive elements must have visible focus states.



Do not rely only on color to show status.



## Code Style



Use:



* functional React components

* hooks

* readable component names

* clear handler names

* small reusable components



Prefer readable code over clever code.



Good names:



```txt

MaintenanceAdmin.jsx

handleStatusChange

appendAuditLog

```



Bad names:



```txt

Page1.jsx

doStuff

x

```



## README Rules



README.md must include:



* project name

* short description

* tech stack

* install command

* run command

* demo credentials

* note that this is frontend-only

* note that security controls are simulated and not certified



## Before Finishing Any Task



Before marking work as complete, verify:



* `npm install` works

* `npm run dev` works

* no missing imports

* no broken routes

* no TODO comments

* no backend calls

* no raw passwords stored

* role guards work

* resident cannot access admin pages

* admin pages render correctly

* mock async operations work

* audit log updates

* mobile layout works

* blue animated interface is preserved
