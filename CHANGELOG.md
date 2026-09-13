# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Contribution workflow:** `CONTRIBUTING.md` writes down how the team works: issue first, a `type/short-description-#issue` branch off `develop`, one pushed commit at a time, a pull request with a merge commit. An issue template and a pull request template carry the formats (#9).
- **Commit Check:** A new workflow runs `.github/scripts/check_commits.py` on every pull request and every push to `develop` and `main`, and fails on a commit that breaks `<type>: <description> #<issue>`. Merge commits, reverts and the commits from before the check are exempt (#9).
- **Dependabot:** `.github/dependabot.yml` checks Maven, npm, GitHub Actions and the Docker base images once a week and opens grouped pull requests against `develop`. Major updates of Maven and npm dependencies stay out of the weekly run until the team plans them, and the Commit Check skips the commits Dependabot writes (#57).

### Changed
- **CI/CD:** Replaced the GitLab pipeline with **GitHub Actions** (`.github/workflows/ci.yml`) after the move of the repository. The job order stays the same: Lint -> Test -> Build -> Package (#1).
    - **Runtimes:** `setup-java` (Temurin 21) and `setup-node` (Node 22) replace the `maven` and `node` container images. Both actions cache the Maven repository and the npm downloads, which covers the old `cache:` block.
    - **Docker:** The Docker-in-Docker service and the wait loop around it are gone. GitHub runners come with a running Docker daemon, so `docker build` works without a service container.
    - **Trigger:** Push builds run on `main` and `develop`, every other branch is covered by the build of its pull request. That replaces the `workflow: rules:` block which kept branch and merge request pipelines from running twice.
    - **Test reports:** GitHub Actions has no counterpart to GitLab's `reports: junit:`. The Surefire XML files go up as a build artifact instead, so a failed test shows up in the job log and in the downloadable report.

### Removed
- **`.gitlab-ci.yml`:** Deleted together with the `deploy` stage, which never held a job (#1).
- **`allow_failure` on the image builds:** The flag worked around the flaky dind setup on the old runners. A broken image build now fails the pipeline (#1).

### Fixed
- **Integration tests on current Linux:** Swapped `de.flapdoodle.embed.mongo.spring30x` 4.11.0 for `de.flapdoodle.embed.mongo.spring3x` 4.20.0. The old artifact resolves a MongoDB download package only up to Ubuntu 23.10, so the three integration tests failed to start their embedded database on Ubuntu 24.04 (#1).
- **Maven Wrapper:** `backend/mvnw` carries the executable bit, so `./mvnw` runs on a fresh clone and in the pipeline (#1).

## [1.0.0] - 2026-02-15

### Added
- **CI/CD Pipeline:** Implemented a robust GitLab CI/CD pipeline (`.gitlab-ci.yml`) featuring:
    - **Linting:** Static code analysis for Frontend (ESLint) and Backend (Maven Validate).
    - **Testing:** Automated Unit and Integration tests with JUnit reporting.
    - **Packaging:** Docker image creation using a specialized Docker-in-Docker (dind) configuration.
- **Frontend Architecture:**
    - Introduced **React Router** (`react-router-dom`) for proper client-side routing.
    - Implemented **AuthContext** (`AuthContext.tsx`) to manage global user state and session persistence.
- **User Features:**
    - **User Profile Page:** New dynamic page (`/user/:username`) displaying a user's avatar and their public media library.
    - **Library Management:** Added ability to save items to the library directly from search results via a new slide-out panel in `MediaCard`.
- **Backend Security:** Implemented **Refresh Token Rotation** logic (`RefreshTokenService`) to securely handle long-lived sessions.
- **Test Infrastructure:** Added `de.flapdoodle.embed.mongo` to allow integration tests to run in isolation without requiring an external database container.

### Changed
- **Backend Architecture:** Refactored project structure to follow a **"Package-by-Feature"** layout (e.g., `feature.auth`, `feature.library`, `feature.search`) for better modularity.
- **Authentication Flow:** Switched from simple JWT returning to **HttpOnly Cookies** (`accessToken`, `refreshToken`) to enhance security and prevent XSS attacks.
- **Docker Configuration:**
    - Optimized `Dockerfile`s using multi-stage builds (reducing image size).
    - Updated `docker-compose.yml` to include **Healthchecks** ensuring the Database is ready before the Backend starts.
    - Adjusted CI/CD Docker jobs to use `tcp://localhost:2375` to fix connection issues on Kubernetes runners.
    - Made Docker build stages non-blocking (`allow_failure: true`) to ensure pipeline stability despite infrastructure fluctuations.
- **Frontend Types:** Enhanced TypeScript definitions in `types.ts` to include `externalId`, `mediaId`, and stricter `User` types.

### Fixed
- **Pipeline Workflow:** Resolved issues where Merge Request pipelines were blocked or duplicated.
- **Integration Tests:** Fixed failing tests in `LibraryControllerTest` and `SearchIntegrationTest` by correcting Mockito matchers and case-sensitivity issues.
- **Frontend Linting:** Resolved various ESLint warnings and removed unsafe `any` casts in `MediaCard.tsx`.
- **Database Consistency:** Fixed `ObjectId` mapping issues between the User entity and the authentication token generation.

## [0.1.1] - 2026-01-19

### Added
- Centralized configuration package (`app.mediatracker.config`) for Mongo and WebClient settings.
- `ManualEntryCommand` pattern in LibraryService to handle complex input parameters cleanly.

### Changed
- **Architecture:** Refactored backend structure to "Package by Feature" (moved search logic to `feature.search`).
- **Refactoring:** Decoupled Authentication logic from `UserService` into a dedicated `AuthService`.
- **Clean Code:** Renamed cryptic variables (e.g., 'q', 'n') in Search Providers to descriptive names for better readability.
- **Security:** Externalized hardcoded JWT secret key to `application.yml`.

### Removed
- Redundant `CorsConfig` class (functionality moved to `SecurityConfig`).
- Obsolete static HTML prototype files (`tech.html`, `lists.html`, `index.html`).
- Unused imports across the backend codebase.

### Fixed
- Critical `NullPointerException` in `JwtFilter` when requests contained no cookies.
- Security vulnerability where sensitive API keys were logged to the console in `RawgClient`.
- CORS configuration issues: Now allows PATCH, DELETE, and OPTIONS requests from the frontend.

## [0.1.0] - 2026-01-14

### Added
- Frontend Search and Login Page
- User Search and User Page Endpoints
- User Login and Registration Endpoints
- Test Data JSONs and Test Data Seeder

## [0.0.1] - 2025-12-01

### Added
- Project skeleton: the Spring Boot backend with Maven Wrapper and entry point under the base package `app.mediatracker`, a base `application.yml`, and the directory layout for backend, frontend, Docker and documentation.
- The first architecture decision record, `docs/architecture/ADRs/ADR 01.md`.

[Unreleased]: https://github.com/Elhan-Salaji/Media-Tracker/compare/1.0.0...HEAD
[1.0.0]: https://github.com/Elhan-Salaji/Media-Tracker/compare/0.1.1...1.0.0
[0.1.1]: https://github.com/Elhan-Salaji/Media-Tracker/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/Elhan-Salaji/Media-Tracker/compare/0.0.1...0.1.0
[0.0.1]: https://github.com/Elhan-Salaji/Media-Tracker/releases/tag/0.0.1
