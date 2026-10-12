# AU Connect

A secure campus platform for students and faculty of Anurag University.

**Features**

- Email verification: only `@anurag.edu.in` addresses can register, and login is blocked until the email is verified
- Department-wise feeds with photo sharing, plus official notices from faculty and admins
- User profiles with a profile picture, department, year and bio
- Help Desk for lost and found item reports, with photos and a "mark resolved" action
- Complaint Desk: complaints are private (only the author and admins see the details), other students see just the title and can vote, and more votes mean a higher priority (LOW, MEDIUM, HIGH, CRITICAL)
- Admin dashboard to review complaints by priority, update their status (Submitted, Under Review, In Progress, Resolved) and reply to the student
- AU Connect logo and a consistent navy and amber visual identity

**Stack:** React 18 + Vite (frontend), Spring Boot 3.3 / Java 21 / Spring Security + JWT / JPA (backend), H2 by default, MySQL optional.

```
au-connect/
├── backend/    Spring Boot API   (port 8080)
└── frontend/   React app         (port 5173)
```

## Prerequisites

- Java 21 and Maven 3.9+
- Node.js 18+ and npm

## Run it

**1. Backend**

```bash
cd backend
mvn spring-boot:run
```

On the first start it creates the database (`backend/data/`) and an admin account:

| Email | Password |
|---|---|
| `admin@anurag.edu.in` | `Admin@12345` |

Change this password before sharing the app: set the `APP_ADMIN_PASSWORD` environment variable before the first start (the account is only created once).

**2. Frontend** (in a second terminal)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The dev server forwards `/api` and `/uploads` to the backend, so there is nothing else to configure.

## How email verification works

1. A user registers with an `@anurag.edu.in` address, and the server emails them a 24-hour link.
2. They open the link, which lands on `/verify?token=...` and marks the account verified.
3. They can now log in. Unverified users get a clear message and a "Resend verification email" button.

**Without SMTP configured** (the default) the link is printed in the backend console and also shown on the registration screen, so you can test the full flow locally. This only happens while `app.dev-mode=true`.

**To send real emails**, uncomment the `spring.mail.*` lines in `backend/src/main/resources/application.properties` (for example Gmail SMTP with an app password), and set `APP_DEV_MODE=false` for production. Also set `APP_FRONTEND_URL` to the public address of the frontend so links in emails point to the right place.

## Configuration

Set these environment variables (or edit `application.properties`):

| Variable | Purpose | Default |
|---|---|---|
| `APP_JWT_SECRET` | Base64 secret (32+ bytes) used to sign login tokens. **Change for production.** | built-in dev secret |
| `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` | First admin account | `admin@anurag.edu.in` / `Admin@12345` |
| `APP_FRONTEND_URL` | Where the frontend runs (CORS and email links) | `http://localhost:5173` |
| `APP_DEV_MODE` | Show the verification link when email is not configured | `true` |

To use **MySQL** instead of H2, swap the three `spring.datasource.*` lines in `application.properties` (an example is included in the file).

Generate a new JWT secret with: `openssl rand -base64 48`

## API overview

All endpoints except `/api/auth/**`, `/api/meta` and `/uploads/**` need `Authorization: Bearer <token>`.

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `GET /api/auth/verify?token=`, `POST /api/auth/resend`, `POST /api/auth/login` |
| Profile | `GET/PUT /api/users/me`, `POST /api/users/me/photo`, `GET /api/users/{id}` |
| Feed | `GET /api/posts?department=CSE`, `POST /api/posts` (multipart, optional photo), `DELETE /api/posts/{id}` |
| Help Desk | `GET /api/helpdesk`, `POST /api/helpdesk` (multipart), `PUT /api/helpdesk/{id}/resolve`, `DELETE /api/helpdesk/{id}` |
| Complaints | `POST /api/complaints`, `GET /api/complaints` (anonymised list), `GET /api/complaints/mine`, `POST /api/complaints/{id}/vote` (toggle) |
| Admin | `GET /api/admin/complaints`, `PUT /api/admin/complaints/{id}`, `GET /api/admin/stats` |

## Privacy and security notes

- Passwords are hashed with BCrypt, and login tokens are signed JWTs that expire after 24 hours.
- Complaint authors and descriptions are only returned to the author and to admins. The student-facing list never includes them.
- A student cannot vote on their own complaint, and each student has one vote per complaint.
- Uploads accept only JPG, PNG, WEBP and GIF (checked by file signature, not just file name), up to 5 MB, and are stored under random names.
- Only faculty and admins can post "Official notice" posts; only admins can use the admin endpoints.

## Project status

This is a working first version. Not yet built: following other users, direct messaging between students, and branch communities. The data model and APIs are structured so these can be added next.

Source was written and reviewed, but it was **not compiled in the environment that generated it** (no access to Maven Central or the npm registry there). If `mvn` or `npm run build` reports anything unexpected on your machine, please send me the message and I will fix it.
