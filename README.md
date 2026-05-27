# GameMatch

GameMatch — backend и web UI для поиска тиммейтов в онлайн-играх. Пользователь создает профиль игрока, добавляет игры и ранги, публикует заявку на поиск напарника, получает отклики, принимает подходящего игрока, после чего система создает временную команду с чатом. После матча участники могут завершить игру и оставить оценки, из которых считается Karma.

Проект реализован на Spring Boot и подходит как учебный full-stack backend-проект: есть REST API, серверный web-интерфейс на Thymeleaf, JWT-аутентификация, роли, админ-панель, Swagger/OpenAPI, миграции БД и интеграционные тесты.

## Стек

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
- Docker Compose для PostgreSQL

## Основные возможности

- Регистрация и вход по email/password.
- Выдача JWT-токена для REST API.
- Web-login через серверную сессию для Thymeleaf-интерфейса.
- Роли `PLAYER` и `ADMIN`.
- Хранение паролей через BCrypt.
- Защита API по ролям.
- Профиль игрока: nickname, timezone, среднее время игры, Karma, количество завершенных матчей.
- Несколько игр в профиле игрока.
- Ранги и роли по каждой игре.
- Поиск профилей по nickname.
- Просмотр чужих профилей, статистики и игр.
- Создание заявок на поиск тиммейта.
- Фильтрация активных заявок по игре, роли, рангу и времени.
- Пагинация списка заявок.
- Отклик на заявку.
- Принятие или отклонение отклика автором заявки.
- Автоматическое создание команды после принятия отклика.
- Чат команды через обычные `POST`/`GET` запросы без WebSocket.
- Завершение матча.
- Оценка тиммейтов и текстовый отзыв.
- Автоматический пересчет Karma.
- Личный кабинет: мои отклики, история матчей, полученные и оставленные отзывы.
- Рейтинг лучших игроков по Karma.
- Админ-панель: список пользователей, блокировка, разблокировка, жалобы, очистка неактивных заявок.
- Жалобы на пользователей.
- Swagger UI и OpenAPI JSON.
- Миграции схемы БД через Flyway.
- Индексы для часто используемых запросов.
- Интеграционные тесты основных сценариев и безопасности.

## Запуск

### 1. Запустить PostgreSQL

```bash
docker compose up -d
```

По умолчанию поднимается PostgreSQL:

- host: `localhost`
- port: `5439`
- database: `gamematch_db`
- user: `postgres`
- password: `1234`

### 2. Настроить переменные окружения

Приложение читает настройки из env-переменных с fallback-значениями:

```properties
DB_URL=jdbc:postgresql://localhost:5439/gamematch_db
DB_USERNAME=postgres
DB_PASSWORD=1234
JWT_SECRET=change-me-to-at-least-32-characters
```

Пример находится в:

```text
src/main/resources/application-example.properties
```

### 3. Запустить приложение

```bash
./mvnw spring-boot:run
```

Для Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Приложение стартует на:

```text
http://localhost:8081
```

## Web UI

Серверный интерфейс реализован на Thymeleaf.

| Страница | Назначение |
| --- | --- |
| `GET /` | Редирект на dashboard или login |
| `GET /login` | Страница входа |
| `POST /login` | Web-вход, создание server-side session |
| `GET /register` | Страница регистрации |
| `POST /register` | Регистрация через web UI |
| `POST /logout`, `GET /logout` | Выход |
| `GET /dashboard` | Главная страница игрока |
| `GET /profiles/me` | Редактирование своего профиля |
| `POST /profiles/me` | Обновление своего профиля |
| `POST /profiles/me/games` | Добавление игры в профиль |
| `POST /profiles/me/games/{playerGameId}` | Обновление игры в профиле |
| `POST /profiles/me/games/{playerGameId}/delete` | Удаление игры из профиля |
| `GET /profiles/search` | Поиск профилей |
| `GET /profiles/{profileId}` | Просмотр чужого профиля |
| `POST /profiles/{profileId}/complaints` | Жалоба на пользователя |
| `GET /requests` | Список активных заявок |
| `GET /requests/my` | Мои заявки |
| `GET /requests/new` | Форма создания заявки |
| `POST /requests` | Создание заявки |
| `GET /requests/{requestId}` | Детальная страница заявки |
| `POST /requests/{requestId}/responses` | Отклик на заявку |
| `POST /responses/{responseId}/accept` | Принять отклик |
| `POST /responses/{responseId}/reject` | Отклонить отклик |
| `POST /requests/{requestId}/cancel` | Отменить свою заявку |
| `GET /teams` | Мои команды |
| `GET /teams/{teamId}` | Команда, чат, отзывы |
| `POST /teams/{teamId}/messages` | Отправить сообщение в чат |
| `POST /teams/{teamId}/complete` | Завершить матч |
| `POST /teams/{teamId}/reviews` | Оценить тиммейта |
| `GET /cabinet` | Личный кабинет |
| `GET /ratings` | Рейтинг игроков по Karma |
| `GET /admin` | Админ-панель |
| `POST /admin/users/{userId}/block` | Заблокировать пользователя |
| `POST /admin/users/{userId}/unblock` | Разблокировать пользователя |
| `POST /admin/complaints/{complaintId}/resolve` | Закрыть жалобу как решенную |
| `POST /admin/complaints/{complaintId}/dismiss` | Отклонить жалобу |
| `POST /admin/requests/inactive/delete` | Удалить неактивные заявки |
| `GET /rank-badges/{game}/{rank}.svg` | SVG-бейдж ранга |

## REST API

REST API использует JWT Bearer authentication:

```http
Authorization: Bearer <token>
```

Публичные эндпоинты:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /v3/api-docs`
- `GET /swagger-ui/index.html`

Остальные API защищены. Игровые эндпоинты доступны ролям `PLAYER` и `ADMIN`, административные — только `ADMIN`.

### Auth API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | public | Регистрация пользователя, создание профиля, выдача JWT |
| `POST` | `/api/auth/login` | public | Вход по email/password, выдача JWT |

### Profile API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `GET` | `/api/profiles/me/{userId}` | self/admin | Получить свой профиль |
| `PUT` | `/api/profiles/me/{userId}` | self/admin | Обновить свой профиль |
| `GET` | `/api/profiles/{profileId}` | player/admin | Получить профиль по ID |
| `GET` | `/api/profiles/search?nickname=` | player/admin | Найти профиль по nickname |
| `GET` | `/api/profiles/suggested?limit=&excludedUserId=` | player/admin | Рекомендованные игроки |
| `GET` | `/api/profiles/me/{userId}/stats` | self/admin | Статистика текущего пользователя |
| `GET` | `/api/profiles/me/{userId}/games` | self/admin | Игры текущего пользователя |
| `POST` | `/api/profiles/me/{userId}/games` | self/admin | Добавить или обновить игру пользователя |
| `PUT` | `/api/profiles/me/{userId}/games/{playerGameId}` | self/admin | Обновить игру пользователя |
| `DELETE` | `/api/profiles/me/{userId}/games/{playerGameId}` | self/admin | Удалить игру пользователя |
| `GET` | `/api/profiles/{profileId}/games` | player/admin | Игры чужого профиля |
| `GET` | `/api/profiles/{profileId}/stats` | player/admin | Статистика чужого профиля |

### Teammate Requests API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `POST` | `/api/requests` | player/admin | Создать заявку |
| `GET` | `/api/requests` | player/admin | Список активных заявок с фильтрами и пагинацией |
| `GET` | `/api/requests/{requestId}` | player/admin | Детали заявки |
| `GET` | `/api/requests/my/{authorId}` | self/admin | Мои заявки |
| `DELETE` | `/api/requests/{requestId}` | player/admin | Отменить свою заявку |
| `DELETE` | `/api/requests/inactive?olderThanDays=7` | admin | Удалить старые неактивные заявки |

Фильтры для `GET /api/requests`:

- `gameId`
- `role`
- `minRank`
- `maxRank`
- `desiredFrom`
- `desiredTo`
- `page`
- `size`
- `sort`

По умолчанию используется пагинация `size=20`, сортировка по `createdAt DESC`.

### Responses API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `POST` | `/api/requests/{requestId}/responses` | player/admin | Откликнуться на заявку |
| `GET` | `/api/requests/{requestId}/responses` | player/admin | Получить отклики по заявке |
| `POST` | `/api/responses/{responseId}/accept` | author/admin | Принять отклик, закрыть заявку, создать команду |
| `POST` | `/api/responses/{responseId}/reject` | author/admin | Отклонить отклик |

### Teams, Matches And Reviews API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `GET` | `/api/teams/{teamId}` | player/admin | Получить команду |
| `GET` | `/api/teams/request/{requestId}` | player/admin | Получить команду по заявке |
| `GET` | `/api/teams/my/{userId}` | self/admin | Мои команды |
| `POST` | `/api/teams/{teamId}/complete` | team member | Отметить матч завершенным |
| `POST` | `/api/teams/{teamId}/reviews` | team member | Оценить тиммейта |
| `GET` | `/api/teams/{teamId}/reviews` | player/admin | Отзывы команды |

### Team Chat API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `POST` | `/api/teams/{teamId}/messages` | team member | Отправить сообщение |
| `GET` | `/api/teams/{teamId}/messages` | team member | Получить историю сообщений |

Чат intentionally простой: без WebSocket, сообщения сохраняются в БД и читаются через polling/обычные HTTP-запросы.

### Player Cabinet API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `GET` | `/api/player/cabinet/responses?status=` | player/admin | Мои отклики, опционально по статусу |
| `GET` | `/api/player/cabinet/history` | player/admin | История сыгранных матчей |
| `GET` | `/api/player/cabinet/reviews/received` | player/admin | Полученные отзывы |
| `GET` | `/api/player/cabinet/reviews/given` | player/admin | Оставленные отзывы |

### Ratings API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `GET` | `/api/ratings/karma?limit=20` | player/admin | Рейтинг игроков по Karma |

### Complaints API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `POST` | `/api/player/complaints` | player/admin | Создать жалобу на пользователя |

### Admin API

| Method | Endpoint | Access | Описание |
| --- | --- | --- | --- |
| `GET` | `/api/admin/dashboard` | admin | Сводка: пользователи, блокировки, жалобы, активные и старые заявки |
| `GET` | `/api/admin/users` | admin | Список пользователей |
| `POST` | `/api/admin/users/{userId}/block` | admin | Заблокировать пользователя |
| `POST` | `/api/admin/users/{userId}/unblock` | admin | Разблокировать пользователя |
| `GET` | `/api/admin/complaints?status=` | admin | Список жалоб |
| `POST` | `/api/admin/complaints/{complaintId}/resolve` | admin | Пометить жалобу решенной |
| `POST` | `/api/admin/complaints/{complaintId}/dismiss` | admin | Отклонить жалобу |

## Swagger / OpenAPI

Документация API генерируется автоматически через `springdoc-openapi`.

После запуска приложения:

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

OpenAPI настроен с Bearer JWT security scheme, поэтому токен можно передать прямо в Swagger UI через кнопку Authorize.

## Модель данных

Основные сущности:

- `User` — учетная запись, email, password hash, роль, флаг блокировки.
- `Role` — роль пользователя: `PLAYER` или `ADMIN`.
- `PlayerProfile` — игровой профиль, nickname, timezone, среднее время игры, Karma, completed matches.
- `Game` — игра.
- `GameRank` — ранги игры с порядком сортировки и badge URL.
- `PlayerGame` — связь профиля с игрой, рангом, ролью и часами игры.
- `TeammateRequest` — заявка на поиск тиммейта.
- `RequestResponse` — отклик игрока на заявку.
- `Team` — временная команда, созданная после принятия отклика.
- `TeamMember` — участник команды.
- `ChatMessage` — сообщение в командном чате.
- `PlayerReview` — оценка и отзыв после матча.
- `Complaint` — жалоба на пользователя.

Схема создается и обновляется через Flyway:

| Migration | Назначение |
| --- | --- |
| `V1__create_users_and_roles.sql` | Пользователи и роли |
| `V2__create_profiles_and_games.sql` | Профили, игры, связи игрок-игра |
| `V3__create_teammate_requests.sql` | Заявки и отклики |
| `V4__create_teams_and_chat.sql` | Команды и чат |
| `V5__create_game_ranks.sql` | Ранги игр |
| `V6__update_rank_badge_urls.sql` | SVG badge URLs для рангов |
| `V7__match_completion_and_reviews.sql` | Завершение матчей и отзывы |
| `V8__completed_match_user_delete_policy.sql` | Политика удаления пользователей для завершенных матчей |
| `V9__create_complaints.sql` | Жалобы |
| `V10__performance_indexes.sql` | Индексы производительности |

## Архитектура

Проект разделен на слои:

```text
controller -> service -> repository -> entity
              mapper
              dto
```

### Controller Layer

Контроллеры принимают HTTP-запросы, валидируют DTO через `@Valid`, получают текущего пользователя через Spring Security и делегируют бизнес-логику в сервисы.

REST-контроллеры находятся в `kz.gamematch.controller.*`.

Web-контроллеры находятся в `kz.gamematch.controller.web.*` и возвращают Thymeleaf templates.

### Service Layer

Сервисы содержат бизнес-правила:

- регистрация и вход;
- проверка прав на self/admin операции;
- создание и фильтрация заявок;
- отклики и принятие отклика;
- создание команды;
- доступ к чату только для участников команды;
- завершение матча;
- создание отзывов;
- пересчет Karma;
- админские действия;
- работа с жалобами.

Транзакционные границы задаются на сервисах через `@Transactional`. Read-only операции помечены `@Transactional(readOnly = true)`, write операции используют обычную транзакцию.

### Repository Layer

Репозитории используют Spring Data JPA:

- стандартные CRUD-методы;
- derived queries;
- `JpaSpecificationExecutor` для фильтрации заявок;
- JPQL-запросы для агрегатов и bulk delete.

### DTO And Mapper Layer

API не возвращает JPA-сущности напрямую. Для внешнего контракта используются DTO:

- `dto.auth`
- `dto.profile`
- `dto.request`
- `dto.response`
- `dto.team`
- `dto.chat`
- `dto.cabinet`
- `dto.admin`

Маппинг вынесен в mapper-компоненты:

- `TeammateRequestMapper`
- `RequestResponseMapper`
- `TeamMapper`
- `ChatMessageMapper`
- `PlayerNicknameResolver`

Такой подход отделяет persistence-модель от API-модели и снижает риск случайно отдать лишние поля, например password hash.

## Использованные принципы и паттерны

### Layered Architecture

Проект построен по классической слоистой архитектуре. Контроллеры не работают напрямую с БД, а репозитории не содержат бизнес-логику.

### DTO Pattern

Для входящих и исходящих данных используются отдельные DTO. Это защищает доменную модель от внешнего API и делает контракт понятным.

### Mapper Pattern

Ручной маппинг сущностей в DTO вынесен в отдельные mapper-классы. Это уменьшает дублирование и делает сервисы чище.

### Repository Pattern

Доступ к данным инкапсулирован в Spring Data repositories.

### Service Layer Pattern

Бизнес-сценарии сгруппированы в сервисах. Например, принятие отклика не просто меняет статус, а закрывает заявку, отклоняет остальные отклики и создает команду.

### Specification Pattern

Фильтрация заявок реализована через `Specification<TeammateRequest>`, что позволяет комбинировать фильтры по игре, роли, рангу и времени без ручной сборки SQL.

### Stateless REST Security

REST API использует JWT и stateless Spring Security filter chain. Токен проверяется на каждом API-запросе.

### Defense In Depth

Админские операции защищены на двух уровнях:

- маршруты `/api/admin/**` доступны только роли `ADMIN` в `SecurityConfig`;
- `AdminService` дополнительно проверяет, что текущий пользователь действительно admin и не заблокирован.

### Transaction Script Для Бизнес-Сценариев

Сложные операции выполняются атомарно внутри транзакций:

- принятие отклика;
- создание команды;
- завершение матча;
- создание отзыва;
- пересчет Karma;
- блокировка пользователя;
- обработка жалобы.

### Fail Fast Validation

Входные DTO валидируются аннотациями Bean Validation, а сервисы дополнительно проверяют бизнес-ограничения: нельзя откликаться на свою заявку, нельзя оценивать себя, нельзя читать чат чужой команды.

## Безопасность

Реализовано:

- BCrypt hashing для паролей.
- JWT Bearer tokens для REST API.
- Stateless API-сессии.
- Разделение доступа по ролям `PLAYER` и `ADMIN`.
- Запрет входа заблокированным пользователям.
- Отклонение JWT заблокированного пользователя.
- Self-or-admin проверки для персональных данных.
- Запрет игроку читать или изменять чужой `/me` профиль.
- Запрет обычному игроку на admin endpoints.
- Запрет админу блокировать самого себя.
- Запрет блокировать admin-аккаунты через admin panel.
- Отключенный `open-in-view`, чтобы доступ к lazy-сущностям контролировался сервисными транзакциями.
- ORM/JPA вместо ручной SQL-склейки, что снижает риск SQL injection.

Важно: web UI использует server-side session, а REST API использует JWT. Это два разных режима входа в одном приложении.

## Производительность

В проекте добавлены индексы для частых сценариев:

- список активных заявок по статусу, игре и времени;
- фильтр заявок по роли;
- личный кабинет по откликам пользователя и статусу;
- рейтинг Karma;
- отзывы, оставленные пользователем.

Файл миграции:

```text
src/main/resources/db/migration/V10__performance_indexes.sql
```

Также используется:

- пагинация для списка заявок;
- read-only транзакции для запросов;
- DTO вместо отдачи entity graph наружу;
- lazy loading в entity-модели с явными транзакционными границами в сервисах.

## Тестирование

Запуск тестов:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

Покрытые сценарии:

- загрузка Spring context;
- генерация и валидация JWT;
- регистрация и логин;
- web-login и рендер dashboard после входа;
- разграничение доступа player/admin;
- запрет доступа к чужим `/me` API;
- блокировка пользователей;
- admin dashboard;
- блокировка и разблокировка пользователей;
- жалобы;
- личный кабинет;
- Karma leaderboard;
- управление играми профиля;
- статистика профиля;
- создание отклика;
- принятие отклика;
- создание команды и чата;
- фильтрация заявок;
- завершение матча;
- отзывы и пересчет Karma.

Текущий полный прогон:

```text
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
```

## Структура проекта

```text
src/main/java/kz/gamematch
  config        OpenAPI configuration
  controller    REST и web controllers
  dto           API request/response DTO
  entity        JPA entities
  mapper        Entity -> DTO mappers
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

## Текущие границы реализации

В текущей версии есть основные сценарии GameMatch: профили, игры, заявки, отклики, команды, чат, отзывы, Karma, личный кабинет, рейтинг, жалобы и админка.

Отдельные standalone-модули для blacklist, notifications и CSV export в коде сейчас не выделены как REST API. В `SecurityConfig` уже зарезервированы защищенные route-группы для таких направлений, но полноценные контроллеры и сервисы под них не реализованы.

## Полезные ссылки после запуска

- Web UI: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
