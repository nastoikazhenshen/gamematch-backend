# GameMatch

GameMatch is a backend and web UI for finding teammates in online games. Players create profiles, add games and ranks, publish teammate search requests, receive responses, accept suitable players, and use temporary teams with a simple chat. After a match, team members can mark it as completed and leave reviews that update Karma.

The project is built with Spring Boot and works as a full-stack backend course project: it includes a REST API, a server-rendered Thymeleaf UI, JWT authentication, roles, an admin panel, Swagger/OpenAPI documentation, Flyway migrations, and integration tests.

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Thymeleaf
- Bean Validation
- Lombok
- JJWT
- springdoc-openapi / Swagger UI
- JUnit 5, Spring Boot Test, Spring Security Test
- Docker Compose for PostgreSQL

## Features

- Registration and email/password login.
- JWT tokens for the REST API.
- Web login with a server-side session for the Thymeleaf UI.
- `PLAYER` and `ADMIN` roles.
- Password hashing with BCrypt.
- Role-based API protection.
- Player profile with nickname, timezone, average play time, Karma, and completed matches.
- Multiple games per profile.
- Rank and main role for each game.
- Profile search by nickname.
- Public profile view with statistics, games, and reviews.
- Teammate request creation.
- Active request filtering by game, role, rank, and desired play time.
- Paginated request list.
- Response submission for a request.
- Response acceptance or rejection by the request author.
- Automatic team creation after response acceptance.
- Team chat through regular `POST` and `GET` requests without WebSocket.
- Match completion.
- Teammate rating with stars and a text review.
- Automatic Karma recalculation.
- Personal cabinet with responses, match history, received reviews, and given reviews.
- Karma leaderboard.
- Admin panel with users, blocking, unblocking, complaints, and inactive request cleanup.
- User complaints.
- Swagger UI and OpenAPI JSON.
- Flyway database migrations.
- Indexes for frequent queries.
- Integration tests for core scenarios and security rules.

## Running Locally

### 1. Start PostgreSQL

```bash
docker compose up -d
```

Default PostgreSQL settings:

- host: `localhost`
- port: `5439`
- database: `gamematch_db`
- user: `postgres`
- password: `1234`

### 2. Configure Environment Variables

The application reads settings from environment variables and provides local fallback values:

```properties
DB_URL=jdbc:postgresql://localhost:5439/gamematch_db
DB_USERNAME=postgres
DB_PASSWORD=1234
JWT_SECRET=change-me-to-at-least-32-characters
```

Example configuration:

```text
src/main/resources/application-example.properties
```

### 3. Start The Application

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Application URL:

```text
http://localhost:8081
```

## Web UI

The server-rendered interface is built with Thymeleaf.

| Page | Purpose |
| --- | --- |
| `GET /` | Redirect to dashboard or login |
| `GET /login` | Login page |
| `POST /login` | Web login and server-side session creation |
| `GET /register` | Registration page |
| `POST /register` | Web registration |
| `POST /logout`, `GET /logout` | Logout |
| `GET /dashboard` | Player dashboard |
| `GET /profiles/me` | Edit current profile |
| `POST /profiles/me` | Update current profile |
| `POST /profiles/me/games` | Add a game to the profile |
| `POST /profiles/me/games/{playerGameId}` | Update a profile game |
| `POST /profiles/me/games/{playerGameId}/delete` | Delete a profile game |
| `GET /profiles/search` | Search players |
| `GET /profiles/{profileId}` | View another player's profile |
| `POST /profiles/{profileId}/complaints` | Report a player |
| `GET /requests` | Active request list |
| `GET /requests/my` | My requests |
| `GET /requests/new` | Create request form |
| `POST /requests` | Create request |
| `GET /requests/{requestId}` | Request details |
| `POST /requests/{requestId}/responses` | Respond to a request |
| `POST /responses/{responseId}/accept` | Accept response |
| `POST /responses/{responseId}/reject` | Reject response |
| `POST /requests/{requestId}/cancel` | Cancel own request |
| `GET /teams` | My teams |
| `GET /teams/{teamId}` | Team page, chat, and reviews |
| `POST /teams/{teamId}/messages` | Send chat message |
| `POST /teams/{teamId}/complete` | Complete match |
| `POST /teams/{teamId}/reviews` | Review teammate |
| `GET /cabinet` | Personal cabinet |
| `GET /ratings` | Karma leaderboard |
| `GET /admin` | Admin panel |
| `POST /admin/users/{userId}/block` | Block user |
| `POST /admin/users/{userId}/unblock` | Unblock user |
| `POST /admin/complaints/{complaintId}/resolve` | Resolve complaint |
| `POST /admin/complaints/{complaintId}/dismiss` | Dismiss complaint |
| `POST /admin/requests/inactive/delete` | Delete inactive requests |
| `GET /rank-badges/{game}/{rank}.svg` | Rank SVG badge |

## REST API

The REST API uses JWT Bearer authentication:

```http
Authorization: Bearer <token>
```

Public endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /v3/api-docs`
- `GET /swagger-ui/index.html`

All other API endpoints are protected. Game endpoints are available to `PLAYER` and `ADMIN`; admin endpoints are available only to `ADMIN`.

### Auth API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | public | Register a user, create a profile, and issue JWT |
| `POST` | `/api/auth/login` | public | Login with email/password and issue JWT |

### Profile API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/profiles/me/{userId}` | self/admin | Get own profile |
| `PUT` | `/api/profiles/me/{userId}` | self/admin | Update own profile |
| `GET` | `/api/profiles/{profileId}` | player/admin | Get profile by ID |
| `GET` | `/api/profiles/search?nickname=` | player/admin | Find profile by nickname |
| `GET` | `/api/profiles/suggested?limit=&excludedUserId=` | player/admin | Suggested players |
| `GET` | `/api/profiles/me/{userId}/stats` | self/admin | Current user statistics |
| `GET` | `/api/profiles/me/{userId}/games` | self/admin | Current user games |
| `POST` | `/api/profiles/me/{userId}/games` | self/admin | Add or update current user game |
| `PUT` | `/api/profiles/me/{userId}/games/{playerGameId}` | self/admin | Update current user game |
| `DELETE` | `/api/profiles/me/{userId}/games/{playerGameId}` | self/admin | Delete current user game |
| `GET` | `/api/profiles/{profileId}/games` | player/admin | Games for another profile |
| `GET` | `/api/profiles/{profileId}/stats` | player/admin | Statistics for another profile |

### Teammate Requests API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/requests` | player/admin | Create request |
| `GET` | `/api/requests` | player/admin | Active request list with filters and pagination |
| `GET` | `/api/requests/{requestId}` | player/admin | Request details |
| `GET` | `/api/requests/my/{authorId}` | self/admin | Own requests |
| `DELETE` | `/api/requests/{requestId}` | player/admin | Cancel own request |
| `DELETE` | `/api/requests/inactive?olderThanDays=7` | admin | Delete old inactive requests |

Filters for `GET /api/requests`:

- `gameId`
- `role`
- `minRank`
- `maxRank`
- `desiredFrom`
- `desiredTo`
- `page`
- `size`
- `sort`

Default pagination uses `size=20` and `createdAt DESC` sorting.

### Responses API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/requests/{requestId}/responses` | player/admin | Respond to a request |
| `GET` | `/api/requests/{requestId}/responses` | player/admin | Get responses for a request |
| `POST` | `/api/responses/{responseId}/accept` | author/admin | Accept response, close request, and create team |
| `POST` | `/api/responses/{responseId}/reject` | author/admin | Reject response |

### Teams, Matches And Reviews API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/teams/{teamId}` | player/admin | Get team |
| `GET` | `/api/teams/request/{requestId}` | player/admin | Get team by request |
| `GET` | `/api/teams/my/{userId}` | self/admin | Own teams |
| `POST` | `/api/teams/{teamId}/complete` | team member | Mark match as completed |
| `POST` | `/api/teams/{teamId}/reviews` | team member | Review teammate |
| `GET` | `/api/teams/{teamId}/reviews` | player/admin | Team reviews |

### Team Chat API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/teams/{teamId}/messages` | team member | Send message |
| `GET` | `/api/teams/{teamId}/messages` | team member | Get message history |

The chat is intentionally simple: no WebSocket, messages are stored in the database and read through polling or regular HTTP requests.

### Player Cabinet API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/player/cabinet/responses?status=` | player/admin | Own responses, optionally filtered by status |
| `GET` | `/api/player/cabinet/history` | player/admin | Completed match history |
| `GET` | `/api/player/cabinet/reviews/received` | player/admin | Received reviews |
| `GET` | `/api/player/cabinet/reviews/given` | player/admin | Given reviews |

### Ratings API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/ratings/karma?limit=20` | player/admin | Karma leaderboard |

### Complaints API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/player/complaints` | player/admin | Create complaint about a user |

### Admin API

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/dashboard` | admin | Summary: users, blocked users, complaints, active requests, and old requests |
| `GET` | `/api/admin/users` | admin | User list |
| `POST` | `/api/admin/users/{userId}/block` | admin | Block user |
| `POST` | `/api/admin/users/{userId}/unblock` | admin | Unblock user |
| `GET` | `/api/admin/complaints?status=` | admin | Complaint list |
| `POST` | `/api/admin/complaints/{complaintId}/resolve` | admin | Mark complaint as resolved |
| `POST` | `/api/admin/complaints/{complaintId}/dismiss` | admin | Dismiss complaint |

## Swagger / OpenAPI

API documentation is generated automatically through `springdoc-openapi`.

After starting the application:

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

OpenAPI is configured with a Bearer JWT security scheme, so the token can be entered in Swagger UI through the Authorize button.

## Data Model

Main entities:

- `User` - account, email, password hash, role, blocked flag.
- `Role` - user role: `PLAYER` or `ADMIN`.
- `PlayerProfile` - game profile, nickname, timezone, average play time, Karma, completed matches.
- `Game` - game.
- `GameRank` - game rank with sort order and badge URL.
- `PlayerGame` - profile-game relation with rank, role, and play hours.
- `TeammateRequest` - teammate search request.
- `RequestResponse` - player's response to a request.
- `Team` - temporary team created after response acceptance.
- `TeamMember` - team member.
- `ChatMessage` - team chat message.
- `PlayerReview` - rating and review after a match.
- `Complaint` - user complaint.

The schema is created and updated through Flyway:

| Migration | Purpose |
| --- | --- |
| `V1__create_users_and_roles.sql` | Users and roles |
| `V2__create_profiles_and_games.sql` | Profiles, games, profile-game relations |
| `V3__create_teammate_requests.sql` | Requests and responses |
| `V4__create_teams_and_chat.sql` | Teams and chat |
| `V5__create_game_ranks.sql` | Game ranks |
| `V6__update_rank_badge_urls.sql` | SVG badge URLs for ranks |
| `V7__match_completion_and_reviews.sql` | Match completion and reviews |
| `V8__completed_match_user_delete_policy.sql` | User delete policy for completed matches |
| `V9__create_complaints.sql` | Complaints |
| `V10__performance_indexes.sql` | Performance indexes |

## Architecture

The project is split into layers:

```text
controller -> service -> repository -> entity
              mapper
              dto
```

### Controller Layer

Controllers receive HTTP requests, validate DTOs with `@Valid`, get the current user from Spring Security, and delegate business logic to services.

REST controllers are located in `kz.gamematch.controller.*`.

Web controllers are located in `kz.gamematch.controller.web.*` and return Thymeleaf templates.

### Service Layer

Services contain business rules:

- registration and login;
- self/admin permission checks;
- request creation and filtering;
- response creation and acceptance;
- team creation;
- chat access only for team members;
- match completion;
- review creation;
- Karma recalculation;
- admin actions;
- complaint processing.

Transactional boundaries are defined on services with `@Transactional`. Read operations use `@Transactional(readOnly = true)`, while write operations use regular transactions.

### Repository Layer

Repositories use Spring Data JPA:

- standard CRUD methods;
- derived queries;
- `JpaSpecificationExecutor` for request filtering;
- JPQL queries for aggregates and bulk delete.

### DTO And Mapper Layer

The API does not expose JPA entities directly. External contracts use DTOs:

- `dto.auth`
- `dto.profile`
- `dto.request`
- `dto.response`
- `dto.team`
- `dto.chat`
- `dto.cabinet`
- `dto.admin`

Entity-to-DTO mapping is extracted into mapper components:

- `TeammateRequestMapper`
- `RequestResponseMapper`
- `TeamMapper`
- `ChatMessageMapper`
- `PlayerNicknameResolver`

This separates the persistence model from the API model and reduces the risk of exposing sensitive fields such as password hashes.

## Principles And Patterns

### Layered Architecture

The project follows a classic layered architecture. Controllers do not access the database directly, and repositories do not contain business logic.

### DTO Pattern

Input and output data use dedicated DTOs. This protects the domain model from the external API and keeps contracts clear.

### Mapper Pattern

Entity-to-DTO mapping is placed in dedicated mapper classes. This reduces duplication and keeps services focused on business logic.

### Repository Pattern

Data access is encapsulated in Spring Data repositories.

### Service Layer Pattern

Business scenarios are grouped in services. For example, accepting a response changes the response status, closes the request, rejects other pending responses, and creates a team.

### Specification Pattern

Request filtering uses `Specification<TeammateRequest>`, making it possible to combine filters by game, role, rank, and time without manual SQL string building.

### Stateless REST Security

The REST API uses JWT and a stateless Spring Security filter chain. The token is validated on every API request.

### Defense In Depth

Admin operations are protected at two levels:

- `/api/admin/**` routes require the `ADMIN` role in `SecurityConfig`;
- `AdminService` additionally verifies that the current user is really an admin and is not blocked.

### Transaction Script For Business Scenarios

Complex operations are performed atomically inside transactions:

- accepting a response;
- creating a team;
- completing a match;
- creating a review;
- recalculating Karma;
- blocking a user;
- resolving a complaint.

### Fail Fast Validation

Input DTOs are validated with Bean Validation annotations, and services also enforce business constraints: a player cannot respond to their own request, cannot review themselves, and cannot read another team's chat.

## Security

Implemented security measures:

- BCrypt password hashing.
- JWT Bearer tokens for the REST API.
- Stateless API sessions.
- Role-based access for `PLAYER` and `ADMIN`.
- Blocked users cannot log in.
- JWT tokens for blocked users are rejected.
- Self-or-admin checks for personal data.
- A player cannot read or modify another user's `/me` profile.
- Regular players cannot access admin endpoints.
- Admins cannot block themselves.
- Admin accounts cannot be blocked from the admin panel.
- `open-in-view` is disabled, so lazy entity access is controlled by service transactions.
- ORM/JPA is used instead of manual SQL concatenation, reducing SQL injection risk.

Important: the web UI uses a server-side session, while the REST API uses JWT. These are two separate login modes in one application.

## Performance

Indexes are added for frequent scenarios:

- active requests by status, game, and time;
- request filtering by role;
- personal cabinet responses by responder and status;
- Karma leaderboard;
- reviews given by a user.

Migration file:

```text
src/main/resources/db/migration/V10__performance_indexes.sql
```

The project also uses:

- pagination for request lists;
- read-only transactions for queries;
- DTOs instead of exposing entity graphs;
- lazy loading in the entity model with explicit service transaction boundaries.

## Testing

Run tests:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

Covered scenarios:

- Spring context loading;
- JWT generation and validation;
- registration and login;
- web login and dashboard rendering after login;
- player/admin access separation;
- blocking access to another user's `/me` API;
- blocked users;
- admin dashboard;
- user blocking and unblocking;
- complaints;
- personal cabinet;
- Karma leaderboard;
- profile games;
- profile statistics;
- response creation;
- response acceptance;
- team and chat creation;
- request filtering;
- match completion;
- reviews and Karma recalculation.

Latest full run:

```text
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
```

## Project Structure

```text
src/main/java/kz/gamematch
  config        OpenAPI configuration
  controller    REST and web controllers
  dto           API request/response DTOs
  entity        JPA entities
  mapper        Entity-to-DTO mappers
  repository    Spring Data repositories
  security      JWT, SecurityConfig, current user helpers
  service       Business logic

src/main/resources
  db/migration  Flyway migrations
  static        CSS, JS, SVG assets
  templates     Thymeleaf pages

src/test/java/kz/gamematch
  security      Security and auth integration tests
  service       Service integration tests
```

## Current Implementation Boundaries

The current version includes the main GameMatch scenarios: profiles, games, requests, responses, teams, chat, reviews, Karma, personal cabinet, leaderboard, complaints, and admin panel.

Standalone modules for blacklist, notifications, and CSV export are not currently implemented as dedicated REST APIs. `SecurityConfig` already reserves protected route groups for these areas, but full controllers and services for them are not present yet.

## Useful Links After Startup

- Web UI: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
