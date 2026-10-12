# Smart Expense Tracker

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB.svg?logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF.svg?logo=vite&logoColor=white)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-38B2AC.svg?logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A production-grade, full-stack personal finance and expense tracking web application. Engineered with a secure **Spring Boot** REST backend, versioned **PostgreSQL** migrations via **Flyway**, stateless **JWT authentication**, and a responsive **React + TypeScript** single-page application styled with **Tailwind CSS**.

---

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Database Schema & Migrations](#database-schema--migrations)
- [REST API Reference](#rest-api-reference)
- [Project Directory Structure](#project-directory-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Configuration](#environment-configuration)
  - [Option 1: Quickstart with Docker Compose](#option-1-quickstart-with-docker-compose)
  - [Option 2: Manual Local Setup](#option-2-manual-local-setup)
- [Automated Testing](#automated-testing)
- [CI/CD Pipeline](#cicd-pipeline)
- [Security Architecture](#security-architecture)
- [License](#license)

---

## Overview

**Smart Expense Tracker** empowers users to take full control of their personal finances through comprehensive transaction logging, category-specific monthly budget caps, spending rhythm analytics, and visual trend breakdowns. 

The application is built around strict data isolation principles: all expenses, custom categories, and budgets are securely scoped to authenticated users with zero cross-tenant leakage.

---

## System Architecture

```
+--------------------------------------------------------------------------+
|                     React 19 SPA (Vite + TypeScript)                     |
|  - Dashboard (KPI Summary, Category Doughnut, 6-Month Trends, Top Items) |
|  - Expense Management (Paginated CRUD, Category/Date Filters, Sorting)   |
|  - Category Management (System Defaults + Custom Categories)             |
|  - Monthly Budgets (Overall & Per-Category Caps, Over-budget Warnings)   |
|  - Financial Analytics (Daily Rhythms, Velocity, Month-over-Month)       |
|  - User Profile & Dark/Light Theme Switching                             |
+------------------------------------+-------------------------------------+
                                     |
                         HTTP / REST (JSON, JWT Bearer)
                                     |
+------------------------------------v-------------------------------------+
|                        Spring Boot 3.3.5 Backend                         |
|  - Stateless Spring Security Filter Chain                                |
|  - JJWT (0.12.6) Authentication & User Context Injection                 |
|  - User-Scoped Isolation across all Repositories & Services              |
|  - Jakarta Bean Validation & Centralized Exception Handling              |
|  - Analytical Trend Aggregators with Zero-Division Defenses              |
+------------------------------------+-------------------------------------+
                                     |
                            Spring Data JPA / SQL
                                     |
+------------------------------------v-------------------------------------+
|                          PostgreSQL 17 Database                          |
|  - Versioned Flyway Migrations (V1 through V6)                           |
|  - Composite Indexes for Ownership, Dates, and Categories                |
|  - Foreign Key Constraints with Cascade Rules                            |
|  - Strict Column Checks (NUMERIC(12,2) > 0, Valid Months & Years)        |
+--------------------------------------------------------------------------+
```

---

## Key Features

### 1. Expense Management
- **Full CRUD Operations**: Record, view, update, and remove expenses with titles, amounts, categories, descriptions, dates, and payment methods.
- **Payment Method Tracking**: Support for UPI, Net Banking, Credit Card, Debit Card, and Cash.
- **Dynamic Search & Filtering**: Filter by category and date ranges (`startDate` to `endDate`).
- **Pagination & Sorting**: Server-side pageable sorting by date and transaction ID with configurable page sizes.
- **Destructive Action Guards**: Confirmation modals protect against accidental deletions.

### 2. Category System & Integrity Protections
- **Dual-Tier Categories**:
  - *System Categories*: Universal defaults (`Food`, `Transport`, `Utilities`, `Entertainment`, `Housing`, `Healthcare`, `Shopping`, `Personal`, `Other`).
  - *Custom Categories*: User-created categories isolated per account.
- **Deletion Blocker**: Prevents deletion of categories actively linked to existing expenses, returning a `409 Conflict` to preserve relational integrity.

### 3. Monthly Budgets & Spending Caps
- **Flexible Budgeting**: Define an overall monthly budget or allocate granular spending caps to specific categories.
- **Real-Time Progress Tracking**: Dynamic visual progress bars calculate actual spending, remaining budget balance, and utilization percentages.
- **Visual Threshold Warnings**: Color-coded states transition from Normal (Green) to Warning (Amber) to Over-Budget (Red).

### 4. Financial Analytics & Visualizations
- **Month-over-Month Comparisons**: Track spending velocity against the previous month.
- **Category Breakdown**: Interactive doughnut charts displaying expenditure distribution.
- **6-Month Historical Trends**: Multi-month spending trajectory crossing calendar year boundaries.
- **Daily Spending Rhythm**: Day-by-day continuous timeline identifying peak spending periods.
- **Top & Recent Transactions**: Quick visibility into high-value purchases and recent activity.

### 5. Authentication & Security
- **Stateless JWT**: Standard Bearer token authentication with configurable expiration.
- **Password Protection**: BCrypt cryptographic hashing for all credentials.
- **Multi-Tenant Scoping**: All database operations query by `user_id` extracted directly from validated JWT claims.

### 6. Modern User Experience
- **Dark & Light Mode**: Persistent theme state with smooth transitions.
- **Responsive Layout**: Designed for mobile, tablet, and desktop viewports.
- **Currency Localization**: Formatted for Indian Rupee (`₹`) by default (`en-IN`), easily adaptable to any locale.

---

## Tech Stack

### Backend
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security 6, JJWT (io.jsonwebtoken: 0.12.6)
- **Data Access**: Spring Data JPA, Hibernate ORM
- **Database Migrations**: Flyway
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito, Spring Security Test

### Database
- **Engine**: PostgreSQL 17
- **Connection Pool**: HikariCP

### Frontend
- **Framework**: React 19
- **Build Tool**: Vite 8
- **Language**: TypeScript
- **Styling**: Tailwind CSS v4
- **Icons**: Lucide React
- **Data Visualization**: Chart.js, react-chartjs-2
- **HTTP Client**: Axios (with centralized interceptors)
- **Testing**: Vitest, React Testing Library, JSDOM

### Infrastructure & DevOps
- **Containerization**: Docker, Docker Compose
- **Web Server & Reverse Proxy**: Nginx (Alpine)
- **CI/CD**: GitHub Actions

---

## Database Schema & Migrations

Database evolution is managed via Flyway migration scripts under `src/main/resources/db/migration/`:

```
db/migration/
├── V1__create_app_health_table.sql
├── V2__create_expenses_table.sql
├── V3__create_users_table_and_user_ownership.sql
├── V4__create_categories_table.sql
├── V5__create_budgets_table.sql
└── V6__add_expenses_user_date_index.sql
```

### Schema Summary

- **`users`**: Stores user credentials (`id`, `name`, `email`, `password_hash`, timestamps). Unique index on `email`.
- **`expenses`**: Financial records (`id`, `user_id`, `title`, `amount`, `category`, `description`, `expense_date`, `payment_method`, timestamps). Indexed on `user_id` and `(user_id, expense_date)`.
- **`categories`**: Category definitions (`id`, `name`, `is_system`, `user_id`, timestamps). Case-insensitive unique indexes distinguish system vs. user custom categories.
- **`budgets`**: Spending caps (`id`, `user_id`, `category_id`, `amount`, `month`, `year`, timestamps). Unique constraints prevent duplicate overall or category budgets for the same month/year.

---

## REST API Reference

All protected endpoints require an `Authorization: Bearer <JWT>` header.

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user account | No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | No |
| `GET` | `/api/auth/me` | Retrieve profile of the current authenticated user | Yes |

#### Sample Registration Request:
```json
POST /api/auth/register
Content-Type: application/json

{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "SecurePassword123!"
}
```

---

### Expenses (`/api/expenses`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/expenses` | Create a new expense | Yes |
| `GET` | `/api/expenses` | List expenses (supports filtering, sorting, pagination) | Yes |
| `GET` | `/api/expenses/{id}` | Retrieve expense details by ID | Yes |
| `PUT` | `/api/expenses/{id}` | Update an existing expense | Yes |
| `DELETE` | `/api/expenses/{id}` | Delete an expense | Yes |

#### Query Parameters for `GET /api/expenses`:
- `category` *(optional)*: Filter by category name (e.g., `Food`).
- `startDate` *(optional)*: Start date in ISO format (`YYYY-MM-DD`).
- `endDate` *(optional)*: End date in ISO format (`YYYY-MM-DD`).
- `page` *(optional, default: `0`)*: Page number (0-indexed).
- `size` *(optional, default: `10`)*: Records per page (range 1–100).

#### Sample Expense Request:
```json
POST /api/expenses
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "Grocery Shopping",
  "amount": 2450.00,
  "category": "Food",
  "description": "Weekly essentials and vegetables",
  "expenseDate": "2026-10-12",
  "paymentMethod": "UPI"
}
```

---

### Categories (`/api/categories`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/categories` | List system categories and user custom categories | Yes |
| `POST` | `/api/categories` | Create a new custom category | Yes |
| `PUT` | `/api/categories/{id}` | Update a custom category name | Yes |
| `DELETE` | `/api/categories/{id}` | Delete a custom category (blocked if in use) | Yes |

---

### Budgets (`/api/budgets`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/budgets` | Set an overall or category budget | Yes |
| `GET` | `/api/budgets?month={m}&year={y}` | List active budgets for a specific month/year | Yes |
| `PUT` | `/api/budgets/{id}` | Update budget target amount | Yes |
| `DELETE` | `/api/budgets/{id}` | Delete a budget entry | Yes |

---

### Analytics (`/api/analytics`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/analytics/summary?month={m}&year={y}` | Total spending, average transaction, MoM change | Yes |
| `GET` | `/api/analytics/category-breakdown?month={m}&year={y}` | Spending aggregated per category with percentages | Yes |
| `GET` | `/api/analytics/monthly-trend?months=6` | Multi-month trend analysis crossing year boundaries | Yes |
| `GET` | `/api/analytics/daily-trend?month={m}&year={y}` | Continuous day-by-day spending rhythm | Yes |
| `GET` | `/api/analytics/recent-expenses?limit=5` | Most recent expense entries | Yes |
| `GET` | `/api/analytics/top-expenses?month={m}&year={y}&limit=5` | Highest-value transactions in the specified period | Yes |
| `GET` | `/api/analytics/budget-status?month={m}&year={y}` | Overall and category budget utilization summaries | Yes |

---

### System Health (`/api/health`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/health` | Comprehensive application and database health check | No |

---

## Project Directory Structure

```
.
├── .github/
│   └── workflows/
│       └── ci.yml               # GitHub Actions CI pipeline
├── docker-compose.yml           # Multi-container orchestration (DB, API, Frontend)
├── Dockerfile                   # Multi-stage backend build (Maven -> Alpine JRE)
├── pom.xml                      # Backend dependencies and plugins
├── mvnw / mvnw.cmd              # Maven wrapper binaries
├── .env.example                 # Root environment template
├── .gitignore                   # Version control ignore rules
├── src/
│   ├── main/
│   │   ├── java/com/smarttracker/
│   │   │   ├── analytics/       # Analytics controllers, services, DTOs
│   │   │   ├── auth/            # Authentication controllers, JWT providers
│   │   │   ├── budget/          # Budget management domain
│   │   │   ├── category/        # Category management domain
│   │   │   ├── expense/         # Core expense domain
│   │   │   ├── health/          # Database health check endpoints
│   │   │   ├── security/        # Spring Security config & filters
│   │   │   └── user/            # User entity and repositories
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Flyway SQL schema migrations (V1 - V6)
│   └── test/                    # Integration and unit tests
├── frontend/
│   ├── Dockerfile               # Multi-stage frontend build (Node -> Nginx)
│   ├── nginx.conf               # Production Nginx reverse-proxy config
│   ├── package.json             # Frontend dependencies & scripts
│   ├── vite.config.ts           # Vite server & API proxy config
│   ├── vitest.config.ts         # Vitest test configuration
│   └── src/
│       ├── api/                 # Axios clients & API services
│       ├── components/          # Reusable UI components & charts
│       ├── context/             # AuthContext & ThemeContext
│       ├── pages/               # Application view routes
│       ├── types/               # TypeScript interface definitions
│       └── utils/               # Currency & date utility helpers
└── README.md                    # Project documentation
```

---

## Getting Started

### Prerequisites

Ensure you have the following installed locally:
- **Java Development Kit (JDK) 21** or higher
- **Node.js 20+** and **npm**
- **PostgreSQL 17** (or Docker Desktop)
- **Git**

---

### Environment Configuration

Copy `.env.example` to `.env` in the project root:

```bash
cp .env.example .env
```

Default configuration variables:

```properties
# PostgreSQL Database
DB_NAME=expense_tracker
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/expense_tracker

# JWT Security
JWT_SECRET=9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b
JWT_EXPIRATION_MS=86400000

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

---

### Option 1: Quickstart with Docker Compose

Run the entire application stack (PostgreSQL, Spring Boot backend, and Nginx-served React frontend) with a single command:

```bash
docker compose up --build
```

- **Frontend Application**: [http://localhost:3000](http://localhost:3000)
- **Backend API**: [http://localhost:8080](http://localhost:8080)
- **API Health Check**: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- **PostgreSQL**: `localhost:5432`

To stop the containers:
```bash
docker compose down
```

---

### Option 2: Manual Local Setup

#### Step 1: Start PostgreSQL
Ensure PostgreSQL 17 is running on port 5432 and create the database:
```sql
CREATE DATABASE expense_tracker;
```
*(Or use Docker for just the database: `docker compose up -d postgres`)*

#### Step 2: Start Spring Boot Backend
Open a terminal in the project root directory:

**Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```
The backend will launch on `http://localhost:8080`. Flyway will automatically execute migrations `V1` through `V6`.

#### Step 3: Start React Frontend
In a new terminal window:

```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser. The Vite development server automatically proxies all `/api` requests to `http://localhost:8080`.

---

## Automated Testing

### Backend Test Suite
The backend includes 61 automated integration and unit tests covering JWT authentication, cross-user tenant isolation, expense CRUD, category constraints, budget logic, and analytical aggregations:

```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### Frontend Test Suite
Component tests and utility tests executed via Vitest:

```bash
cd frontend
npm test -- --run
```

### Production Build Validation
```bash
cd frontend
npm run build
```

---

## CI/CD Pipeline

The project features a continuous integration workflow defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml). On every push or pull request to `main` or `master`:

1. **Backend Job**:
   - Boots an ephemeral PostgreSQL 17 service container.
   - Sets up JDK 21 with Temurin distribution.
   - Executes all unit and integration tests (`./mvnw test`).
2. **Frontend Job**:
   - Sets up Node.js 22 with dependency caching.
   - Installs packages via `npm ci`.
   - Runs frontend tests (`npm test -- --run`).
   - Compiles production distribution artifacts (`npm run build`).

---

## Security Architecture

- **Stateless Session Management**: `SessionCreationPolicy.STATELESS` ensures no server-side HTTP session state is stored.
- **Token Verification**: Custom `JwtAuthenticationFilter` intercepts incoming requests, extracts Bearer tokens, validates signatures using HMAC-SHA256, and populates the `SecurityContextHolder`.
- **Credential Storage**: Passwords are encrypted using Spring Security's `BCryptPasswordEncoder` with a default strength factor.
- **Tenant Isolation**: Every database query and write operation filters strictly by the authenticated `userId`. Attempts to access or mutate records belonging to other users result in an immediate `404 Not Found` or `403 Forbidden`.
- **Input Validation**: Request payloads are validated with Jakarta annotations (`@NotBlank`, `@Positive`, `@Email`, etc.) before reaching business logic.
- **CORS Protection**: Allowed origins are restricted and configurable via environment variables.

---

## License

This project is licensed under the [MIT License](LICENSE).
"# smartexpense_tracker" 
