# Technical Specification

## Technical Context & Complexity
- Stack: Vue 3 + TypeScript + Vite, Vue Router, Pinia, Axios for API; styling with CSS/SCSS and design tokens to match provided UI. Backend: Java 17+ with Spring Boot (Web, Security), Spring Data JPA, MySQL, JWT auth. Password hashing with BCrypt.
- Deployment shape: separate `frontend/` and `backend/` projects, backed by a MySQL database. HTTP-only cookie for JWT, optional refresh token if needed.
- Complexity: Medium–High — full auth flows (login, logout, forgot password, register), JWT security, and a content feed UI matching the screenshot.

## Implementation Approach
- Frontend
  - Scaffold Vite + Vue 3 + TS app with routes: `/login`, `/register`, `/forgot-password`, `/` (feed/home). Guard protected pages using JWT presence and backend validation.
  - Global state via Pinia: auth store (user profile, token presence flag, login/logout actions), UI store (filters/search chips state).
  - Axios instance with base URL, JSON headers, auth cookie mode, 401 interceptor to redirect to login.
  - Layouts/components to mirror screenshot: header/title bar, filter chip list, feed cards, sidebar widgets (top reviewers, featured brands, most viewed). Responsive grid (2-column desktop, stacked mobile) with consistent spacing, shadows, rounded corners. Use CSS variables for palette/typography.
  - Forms: login/register/forgot-password with validation (email format, password rules, confirmation match). Show inline errors from backend responses.

- Backend
  - Spring Boot project with modules: domain entities, repositories, services, controllers, security config, DTO mapping, exception handling.
  - JWT flow: login issues JWT (short-lived) set in HTTP-only cookie; logout clears cookie; auth filter extracts/validates token; password hashing via BCryptPasswordEncoder; optional refresh token endpoint if needed for session longevity.
  - Forgot password: endpoint to request reset token, persist hashed token with expiry; endpoint to reset password by token. (Email sending can be abstracted via service interface with stub implementation if SMTP not configured.)
  - Data services for reviews, brands, reviewers rankings. Use pageable queries with filters (price range, fuel type, brand, sorting). Seed/dev fixtures for UI demonstration if real data absent.

- Security/Auth
  - Endpoints open: `POST /auth/login`, `POST /auth/register`, `POST /auth/forgot-password`, `POST /auth/reset-password`, read-only content APIs (reviews/brands/top reviewers) can stay public. Protected endpoints (e.g., posting reviews) require `ROLE_USER` via JWT filter.
  - CORS configured to allow frontend origin, credentials enabled for cookie auth. CSRF disabled for stateless APIs.

## Source Code Structure Changes
- Root directories: `frontend/` (Vite Vue app) and `backend/` (Spring Boot project).
- Frontend structure:
  - `src/main.ts`, `src/App.vue`, `src/router/index.ts`, `src/stores/auth.ts`, `src/stores/ui.ts`.
  - Components: `components/FilterChips.vue`, `components/ReviewCard.vue`, `components/Sidebar/TopReviewers.vue`, `components/Sidebar/FeaturedBrands.vue`, `components/Sidebar/MostViewed.vue`, `components/Form/AuthForm.vue`.
  - Views: `views/Login.vue`, `views/Register.vue`, `views/ForgotPassword.vue`, `views/Feed.vue`.
  - Styling: `src/styles/variables.scss`, `src/styles/base.scss` for theming consistent with screenshot.
- Backend structure:
  - `src/main/java/.../config` (security config, CORS), `security` (JWT filter, util), `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`.
  - Entities: `User`, `Review`, `VehicleBrand`, `ReviewerStats`, `PasswordResetToken`.
  - Repositories: JPA repositories for above entities; custom queries for rankings and featured lists.
  - Controllers: `AuthController`, `ReviewController`, `BrandController`, `ReviewerController`.
  - Services: auth (register/login/logout/reset), review feed retrieval with filters, reviewer ranking, brand list, password reset token handling.

## Data Model / API / Interfaces
- User: id, username, email, passwordHash, avatarUrl, roles, createdAt, updatedAt, stats (followers, rating average, reviewCount optional).
- Review: id, title, slug, content excerpt, author (User), vehicleBrand, vehicleModel/year, heroImageUrl, likes, commentsCount, views, tags/filters (fuel type, price segment).
- VehicleBrand: id, name, logoUrl, featured (bool), sortOrder.
- ReviewerStats: aggregates for sidebar (userId, displayName, avatarUrl, followers, ratingAvg).
- PasswordResetToken: tokenHash, userId, expiresAt, used flag.
- API (initial set):
  - `POST /auth/register` (body: username, email, password, confirm)
  - `POST /auth/login` (body: email, password) → sets JWT cookie, returns user profile
  - `POST /auth/logout` → clears auth cookie
  - `POST /auth/forgot-password` (body: email) → issues reset token (email/stub response)
  - `POST /auth/reset-password` (body: token, newPassword)
  - `GET /reviews` (query: brand, priceRange, fuelType, sort, page) → list for feed
  - `GET /reviews/{id}` (optional for detail)
  - `GET /reviewers/top` → sidebar list
  - `GET /brands/featured` → sidebar list
  - (Optional) `POST /reviews` for authenticated creation if needed.

## Verification Approach
- Frontend: `npm run lint`, `npm run test` (unit with Vitest), `npm run build` for production bundle. Add these scripts during scaffolding and run them before delivery.
- Backend: `mvn -B verify` (runs unit tests), `mvn test` for quick cycle. Include minimal tests for auth service and review query service. Use Testcontainers or embedded DB for repo tests.
- Manual: exercise login/register/logout, forgot/reset flows, and verify feed + sidebars match screenshot layout/responsiveness (desktop/mobile). Ensure JWT cookie present and cleared on logout.
