
# AssetTrack

AssetTrack is an asset inventory and allocation management application built with a Spring Boot backend and a React frontend. The project supports user authentication, role-based user management, asset CRUD operations, allocation workflows, condition reporting, dashboard metrics, reports, and notification handling.

## Team (7)

| Full Name | ID Number |
|---|---:|
| Nayera Sherif | 45 |
| Samar Hatem | 22 |
| Shahd Ayman | 25 |
| Rahma Fathy | 18 |
| Haneen Mohamed | 16 |


## Features

### Backend
- JWT-based authentication via `/api/auth/register` and `/api/auth/login`
- Role-based access control for ADMIN, MANAGER, and DEVELOPER
- User management endpoints for listing, creating, updating, role changes, and deleting users
- Asset management endpoints for creating, reading, updating, and deleting assets
- Allocation workflows: assign, return, transfer, current owner lookup, and asset history
- Asset search and spare laptop lookup
- Condition reporting for assets with admin/manager review and update
- Notification endpoints with read, mark all read, delete, low-stock, and warranty check support
- Dashboard and usage statistics endpoints
- Dev seed endpoint to populate sample users and assets for testing

### Frontend
- React single-page application using protected routes
- Login and registration flows with JWT storage in `localStorage`
- Role-aware sidebar navigation and UI visibility
- Asset list with filtering, pagination, create/edit/delete for authorized roles
- Asset allocation console for lookup, assign, return, and transfer operations
- User management page for ADMIN role
- Notifications page with category filtering, mark-as-read, and delete actions
- Reports page showing usage metrics and condition reports

## Tech Stack
- Java 17
- Spring Boot 3.5
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- PostgreSQL JDBC Driver
- JJWT for JWT handling
- Lombok
- React 19
- React Router DOM 7
- Create React App / react-scripts
- Axios for HTTP requests

## Project Structure

```
backend/assesttrack/
  pom.xml
  src/main/java/com/assettrack/allocation/
    config/
    controller/
    dto/
    entity/
    exception/
    mapper/
    repository/
    security/
    service/
  src/main/resources/application.properties

frontend/
  package.json
  public/
  src/
    api/
    components/
      allocation/
      layout/
      users/
    pages/
      auth/
      users/
    services/
    styles/
```

## Prerequisites
- Java 17
- Maven 3.x
- Node.js and npm
- PostgreSQL database
- Optional: Mailtrap account for email testing

## Setup Instructions

### Backend
1. Open a terminal and go to the backend module:

```bash
cd backend/assesttrack
```

2. Update `src/main/resources/application.properties` with your database and JWT secret values.
3. Build the backend:

```bash
mvn clean package
```

4. Run the backend:

```bash
mvn spring-boot:run
```

By default, the backend listens on port `8080`.

### Frontend
1. Open a terminal and go to the frontend folder:

```bash
cd frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start the frontend:

```bash
npm start
```

The frontend runs on `http://localhost:3000` by default and proxies API requests to `http://localhost:8080`.

If you need to override the backend URL, set:

```bash
REACT_APP_API_URL=http://localhost:8080 npm start
```

### Database Configuration
The backend configuration is located at `backend/assesttrack/src/main/resources/application.properties`.
Update the following properties for your PostgreSQL instance:

```properties
spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.datasource.driver-class-name=org.postgresql.Driver
```

This project uses `spring.jpa.hibernate.ddl-auto=update` to synchronize schema changes during development.

## Environment Variables

### Backend properties
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.driver-class-name`
- `spring.jpa.hibernate.ddl-auto`
- `jwt.secret`
- `jwt.expiration`
- `app.jwt.secret`
- `spring.mail.host`
- `spring.mail.port`
- `spring.mail.username`
- `spring.mail.password`
- `spring.mail.properties.mail.smtp.auth`
- `spring.mail.properties.mail.smtp.starttls.enable`
- `notifications.email.enabled`
- `notifications.email.from`
- `notifications.email.to`
- `notifications.warranty-warning-days`
- `notifications.low-stock-threshold`
- `notifications.low-stock-types`

### Frontend environment variable
- `REACT_APP_API_URL` — optional API base URL for the React app

> Note: `JwtService` in the backend loads `app.jwt.secret`; if it is not set, a temporary signing key is generated at startup.

## API Overview

### Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`

### User management
- `GET /api/users`
- `GET /api/users/{id}`
- `GET /api/users/by-role?role=DEVELOPER`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PATCH /api/users/{id}/role`
- `DELETE /api/users/{id}`

### Asset management
- `POST /api/assets`
- `GET /api/assets`
- `GET /api/assets/{id}`
- `PUT /api/assets/{id}`
- `DELETE /api/assets/{id}`

### Allocation workflows
- `POST /api/allocations/assign`
- `POST /api/allocations/{id}/return`
- `POST /api/allocations/transfer`
- `GET /api/allocations/asset/{assetId}/history`
- `GET /api/allocations/current-owner/{assetId}`
- `GET /api/allocations/spare-laptops`

### Search
- `POST /api/search`
- `GET /api/search/spare-laptops`

### Dashboard and reports
- `GET /api/dashboard`
- `GET /api/reports/usage-statistics`

### Notifications
- `GET /api/notifications`
- `POST /api/notifications/{id}/read`
- `POST /api/notifications/mark-all-read`
- `DELETE /api/notifications/{id}`
- `GET /api/notifications/test`
- `GET /api/notifications/test-email`
- `GET /api/notifications/trigger-warranty-check`
- `GET /api/notifications/low-stock-counts`
- `POST /api/notifications/migrate-legacy`

### Asset condition reports
- `POST /api/condition-reports`
- `GET /api/condition-reports`
- `PUT /api/condition-reports/{id}`
- `GET /api/condition-reports/my`

### Dev seed
- `POST /api/dev/seed`

## Default Data

The backend includes a dev seed endpoint that creates sample users and assets if they do not already exist.

### Seed users
- `admin@assettrack.com` — ADMIN
- `manager@assettrack.com` — MANAGER
- `ahmed@assettrack.com` — DEVELOPER
- `sara@assettrack.com` — DEVELOPER

### Seed assets
- `SN-DELL-001` — Dell XPS 15
- `SN-APPLE-002` — MacBook Pro 14
- `SN-MX-003` — Logitech MX Master 3
- `SN-OLD-004` — Old Thinkpad (Expired)

## How to Run the Project

1. Start the backend from `backend/assesttrack`.
2. Start the frontend from `frontend`.
3. Open `http://localhost:3000` in your browser.
4. Register a new account or use the dev seed endpoint to create sample users.
5. Use the sidebar to navigate Dashboard, Assets, Allocation, Users, Reports, and Notifications.

