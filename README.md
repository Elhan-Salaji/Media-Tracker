# Media Tracker 3

[![CI](https://github.com/Elhan-Salaji/Media-Tracker/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/Elhan-Salaji/Media-Tracker/actions/workflows/ci.yml)

Media Tracker keeps anime, manga, movies, series, games, books and music in one personal library. One search covers all seven media types across five public APIs. You save a title with a status and notes, and your list shows up on a public profile page.

This README covers running the project. The [wiki](https://github.com/Elhan-Salaji/Media-Tracker/wiki) explains why it looks the way it does, starting with the [Architecture Decision Records](https://github.com/Elhan-Salaji/Media-Tracker/wiki/Architecture-Decision-Records).

## Stack

| Part | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Security with JWT in HttpOnly cookies |
| Database | MongoDB 7 |
| Frontend | React 19, TypeScript, Vite, Bootstrap |
| Search sources | Jikan (anime, manga), IMDb API (movies, series), RAWG (games), Open Library (books), iTunes (music) |
| Build and CI | Maven Wrapper, npm, Docker Compose, GitHub Actions |

## Repository layout

```
backend/    Spring Boot service (./mvnw)
frontend/   React app built with Vite
docker/     docker-compose.yml for the full stack
docs/       use case diagram
```

## Run it with Docker

You need Docker with Compose v2.

```bash
git clone https://github.com/Elhan-Salaji/Media-Tracker.git
cd Media-Tracker
cp backend/.env.example backend/.env    # fill in JWT_SECRET and RAWG_API_KEY
cd docker
docker compose up -d --build
```

Three containers start: `mt3-mongo`, `mt3-backend` and `mt3-frontend`. Open the app at http://localhost:5173 and register an account. The backend answers on http://localhost:8080, and its API documentation sits at http://localhost:8080/swagger-ui.html.

On the first start the backend seeds 20 demo users with library entries, so the public profile pages have content. The demo users have no working password.

## Configuration

The backend refuses to start while a required value is missing.

| Variable | Where you set it | Meaning |
|---|---|---|
| `JWT_SECRET` | `backend/.env` | Signs the access and refresh tokens. Generate one with `openssl rand -base64 48`. |
| `RAWG_API_KEY` | `backend/.env` | Key for the game search, free at https://rawg.io/apidocs. The backend only asks for it while game search is enabled. |
| `VITE_API_BASE_URL` | `frontend/.env` for `npm run dev`, your shell or `docker/.env` for Docker Compose | Address of the backend as the browser sees it. Defaults to `http://localhost:8080`. Vite writes it into the bundle at build time, so a change needs a new build. |

The `.env` files stay out of git. `backend/.env.example` and `frontend/.env.example` list the variables.

## Run it from source

You need Java 21 or newer, Node 24 and Docker for the database.

```bash
# MongoDB
cd docker && docker compose up -d mongo

# Backend on http://localhost:8080, reads backend/.env
cd backend && ./mvnw spring-boot:run

# Frontend on http://localhost:5173
cd frontend && npm ci && npm run dev
```

## Tests and checks

```bash
cd backend && ./mvnw test                    # unit and integration tests, embedded MongoDB
cd frontend && npm run lint && npm run build # lint and type check
```

GitHub Actions runs the same checks on every pull request and builds both Docker images.

## Contributing

Every change starts with an issue and reaches `develop` through a pull request. [CONTRIBUTING.md](CONTRIBUTING.md) describes the branch names, the commit format and the checklist. [CHANGELOG.md](CHANGELOG.md) lists the changes per version.

## License

Media Tracker is released under the [MIT License](LICENSE).
