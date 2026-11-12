# CURRENT TASK: Fix nginx 403 on Staging & Prod frontends via In-Container Flutter Builds

## Diagnosis (inspected on staging)
- Staging nginx error: `directory index of "/usr/share/nginx/html/" is forbidden` -> `/usr/share/nginx/html` has **no `index.html`**.
- `HouseMate-Staging/frontend-web/` contains only a stub `manifest.json`.
- `frontend-web/` is gitignored (`.gitignore:62`). The Flutter PWA was previously built manually on the laptop and rsynced. It was never delivered to the new separated directory layout.
- Both environments experience 403s due to missing static Flutter web build artifacts in their respective `frontend-web` directories.

## Constraints
- Never modify tracked Flutter static source files (`mobile/web/manifest.json`). Never commit build artifacts.
- Remote ops strictly confined to `~/.gemini/config/skills/t480-ops/scripts/t480-jail.sh`.
- Deploy and verify staging first; do not deploy to production without explicit user confirmation.
- Work on `feat/*` or `dev` following the Git promotion flow: `feat/*` -> `dev` -> `staging` -> `main`.

## Build-on-t480 Architecture & Decisions
- **Host Tools**: t480 has Docker 29.1.3, git, curl, unzip, xz. Partitions were expanded/corrected by the user (disk space is healthy).
- **Decision**: Build Flutter entirely inside a multi-stage Docker build (`frontend.Dockerfile`), matching the backend's Docker pattern. No host Flutter/Dart installation required.
- **Flutter Image**: `ghcr.io/cirruslabs/flutter:3.47.9` (matches CI `pr-validation.yml`).
- **Confirmed Staging API URL**: `https://selfhost.tailff80a9.ts.net/api`.
- **Default Prod API URL**: `https://api.housemateapp.stream/api`.

---

## Step-by-Step Execution Plan

- [ ] 1. **Create `frontend.Dockerfile`** (repo root):
  - Stage 1 (build):
    ```dockerfile
    FROM ghcr.io/cirruslabs/flutter:3.47.9 AS build
    ARG API_BASE_URL=https://api.housemateapp.stream/api
    WORKDIR /app/mobile
    COPY mobile/ .
    RUN flutter pub get && flutter build web --release --dart-define=API_BASE_URL=${API_BASE_URL}
    ```
  - Stage 2 (serve):
    ```dockerfile
    FROM nginx:alpine
    COPY --from=build /app/mobile/build/web /usr/share/nginx/html
    COPY nginx.conf /etc/nginx/conf.d/default.conf
    EXPOSE 80
    CMD ["nginx", "-g", "daemon off;"]
    ```
  - Ensure `.dockerignore` ignores `mobile/build/` and `mobile/.dart_tool/`.

- [ ] 2. **Configurable API URL in Dart (`mobile/lib/core/network/api_client.dart`)**:
  - Update `baseUrl`:
    ```dart
    static const String baseUrl = String.fromEnvironment(
      'API_BASE_URL',
      defaultValue: 'https://api.housemateapp.stream/api',
    );
    ```
  - Run existing test suites for mobile using `silent-test-runner` or `flutter test --reporter compact test/core/network/api_client_test.dart` (if present).

- [ ] 3. **Update `nginx.conf` (repo root)**:
  - Add safe dynamic reverse proxy for `/api` that resolves via Docker DNS (`127.0.0.11`) without failing on startup:
    ```nginx
    location ^~ /api {
        resolver 127.0.0.11 valid=30s;
        set $backend_upstream http://backend:8080;
        proxy_pass $backend_upstream;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    ```

- [ ] 4. **Update `docker-compose.staging.yml`**:
  - Point `frontend` service to build from `frontend.Dockerfile`:
    ```yaml
    frontend:
      build:
        context: .
        dockerfile: frontend.Dockerfile
        args:
          API_BASE_URL: https://selfhost.tailff80a9.ts.net/api
      restart: unless-stopped
      ports:
        - "8091:80"
      command: >
        sh -c "sed -i 's/\"name\": \"HouseMate\"/\"name\": \"HouseMate - Staging\"/g; s/\"short_name\": \"HouseMate\"/\"short_name\": \"HM Staging\"/g' /usr/share/nginx/html/manifest.json && nginx -g 'daemon off;'"
      depends_on:
        - backend
    ```
  - Remove `./frontend-web` bind mount and `./nginx.conf` mount (both are now baked into the image).
  - Preserve `backend` port mappings (`127.0.0.1:8092:8080` and `127.0.0.1:9092:9092`).

- [ ] 5. **Update `docker-compose.prod.yml`**:
  - Replace `image: nginx:alpine` and `./frontend-web` bind mount with:
    ```yaml
    frontend:
      build:
        context: .
        dockerfile: frontend.Dockerfile
      restart: unless-stopped
    ```
  - Remove `./frontend-web` and `./nginx.conf` bind mounts (baked into image).

- [ ] 6. **Update CI Workflow (`.github/workflows/pr-validation.yml`)**:
  - In `build-and-test` job, add web compilation verification after tests:
    ```yaml
    - name: Verify Flutter Web Build
      run: |
        cd mobile
        flutter build web --release --dart-define=API_BASE_URL=https://dummy.stream/api
    ```
  - Add compose schema validation step:
    ```yaml
    - name: Validate Staging and Prod Docker Compose configs
      run: |
        APP_INVITE_CODE=ci JWT_SECRET_KEY=ci docker compose -f docker-compose.yml -f docker-compose.staging.yml config -q
        APP_INVITE_CODE=ci JWT_SECRET_KEY=ci DB_USERNAME=ci DB_PASSWORD=ci TUNNEL_TOKEN=ci docker compose -f docker-compose.yml -f docker-compose.prod.yml config -q
    ```

- [ ] 7. **Update `t480-ops` Skill (`~/.gemini/config/skills/t480-ops/SKILL.md`)**:
  - Update layout table: `frontend` is built via `frontend.Dockerfile` rather than mounted from `frontend-web`.
  - Add build & deployment recipes (`compose-staging "up -d --build"` handles both backend and frontend).
  - Add Docker image pruning housekeeping recipe (`docker image prune -f`).

- [ ] 8. **Local Validation**:
  - Run compose validation for both staging and prod configs with dummy environment variables.
  - Do NOT run full Flutter Docker build locally on laptop to conserve local resources.

- [ ] 9. **Git Promotion & Staging Deployment**:
  - Commit changes to branch `feat/docker-frontend-build` and merge to `dev`.
  -  PR to `staging`.
  - On t480 Staging: `git pull origin staging` in `HouseMate-Staging`.
  - Clean up obsolete `HouseMate-Staging/frontend-web/` stub directory.
  - Run `compose-staging "up -d --build"`.
  - Verify container status with `compose-staging ps`.
  - Verify logs (`compose-staging "logs --tail 30"`), ensuring 200 responses on PWA requests and successful branding in `/usr/share/nginx/html/manifest.json`.

- [ ] 10. **Production Deployment (Wait for User Confirmation)**:
  - After user verification of staging, merge `staging` -> `main`.
  - Pull and run `compose-prod "up -d --build"`.
  - Verify prod health.

---

## Verification & Tests
- **Mobile Unit Tests**: Run via `silent-test-runner` (`flutter test --reporter compact`).
- **Compose Linting**: `docker compose config -q` for both staging and prod stacks.
- **Staging Verification**: Inspect container boot logs and verify `manifest.json` dynamic branding.
