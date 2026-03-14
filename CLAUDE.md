# CLAUDE.md — MoneyCol Repository Guide

This file provides context for AI assistants (Claude Code, etc.) working in this repository.

---

## Project Overview

**MoneyCol** is a coin and banknote collection management application. It lets users:
- Search and browse a catalog of banknotes (by country, year, name)
- Log in via Google OAuth
- Create and manage personal collections and wishlists

---

## Repository Structure

```
moneycol/
├── server/              # GraphQL API server (TypeScript, Lerna monorepo)
├── frontend/            # React SPA (TypeScript, Material-UI)
├── collections/         # Java collections service (Gradle multi-module)
├── data-collector/      # Node.js crawler for ingesting catalog data
├── elasticsearch/       # Elasticsearch config and data
├── infra/               # Infrastructure as Code (Pulumi + Cloud Functions)
├── .github/workflows/   # GitHub Actions CI
└── docker-compose.yml   # Local Elasticsearch for development
```

---

## Components In Detail

### 1. `server/` — GraphQL API (TypeScript, Lerna)

A Lerna monorepo managed with Yarn workspaces. Node.js 14+ required.

**Packages:**
- `server/packages/graphql/` — Apollo Server 2, main entry point, port 4000
- `server/packages/auth/` — Firebase authentication
- `server/packages/users/` — Firestore user management

**Key commands (run from `server/`):**
```bash
yarn               # Install dependencies
yarn build         # Compile all packages (generates dist/ and copies schema)
yarn start         # Start compiled server
yarn start:dev     # Hot reload via nodemon + ts-node (requires FIREBASE_API_KEY, etc.)
yarn test          # Run Jest tests across all packages
yarn clean         # Remove build artifacts
```

**Dev server with hot reload:**
```bash
FIREBASE_API_KEY=XXXX \
ELASTICSEARCH_ENDPOINT_WITH_PORT=localhost:9200 \
COLLECTIONS_API_HOST=localhost:8080 \
yarn start:dev
```

GraphQL playground available at http://localhost:4000/graphql

**Important: run `yarn build` before `yarn start:dev`** — the `dist/schema` directory must exist.

**Hot reload setup:** For `nodemon` to pick up changes in `auth` or `users` packages, start incremental compilation in those packages:
```bash
cd server/packages/auth && yarn compile:watch
cd server/packages/users && yarn compile:watch
```

**Environment variables:**
- `FIREBASE_API_KEY` — Firebase project key
- `ELASTICSEARCH_ENDPOINT_WITH_PORT` — e.g. `localhost:9200`
- `COLLECTIONS_API_HOST` — Collections API host, e.g. `localhost:8080`

**TypeScript:** Version 4.6.3. Config files: `server/tsconfig.json`, `server/tsconfig.build.json`.

---

### 2. `frontend/` — React SPA (TypeScript)

Built with Create React App. Uses a git submodule (`crema_template-2.0`) for the UI template.

**Key commands (run from `frontend/`):**
```bash
npm install        # Install dependencies
npm start          # Dev server
npm run build      # Production build
npm run build:staging  # Staging build (via env-cmd)
npm test           # Jest + React Testing Library
npm run test-ci    # CI test run
npm run start:webserver  # Serve via Express
npm run start:backend    # Start GraphQL server + Elasticsearch
```

**Submodule:** The `crema_template-2.0/` directory is a git submodule. When cloning:
```bash
git submodule update --init --recursive
```

**Key libraries:** React 16.12, Apollo Client 2.6.4, Redux 4, Material-UI 4.9, Formik 2.

**TypeScript:** Version 3.7.2. Config: `frontend/tsconfig.json`.

---

### 3. `collections/` — Java Collections Service (Gradle)

A Gradle multi-module Java 11 project.

**Submodules:**
- `collections-api/` — REST API for managing collections (port 8080)
- `data-collector/` — Java-based data ingestion from Colnect
- `data-indexer/` — Elasticsearch indexing service

**Key commands (run from `collections/`):**
```bash
./gradlew build                          # Full build
./gradlew :collections-api:test         # API tests
./gradlew :collections-api:jib          # Build Docker image via Jib
./gradlew clean                          # Remove build artifacts
```

**Testing:** Uses JUnit + TestContainers (Firestore emulation). Docker must be installed.

**Required environment variables for tests:**
```bash
export FIREBASE_API_KEY=<value>
export GOOGLE_APPLICATION_CREDENTIALS=<path-to-service-account.json>
```

**Code generation:** Lombok annotations used throughout.

---

### 4. `data-collector/crawler/` — Node.js Crawler

Crawls catalog data from external sources and writes to GCS. Deployed as a Kubernetes CronJob.

---

### 5. `infra/` — Infrastructure as Code

- `infra/gke/` — Pulumi TypeScript project for GKE cluster provisioning
- `infra/gke-resizer/` — Cloud Function to scale the GKE cluster on a schedule (Cloud Scheduler)

---

## Local Development Setup

### Prerequisites
- Node.js 14+ and Yarn (server)
- Node.js 12+ and npm (frontend)
- Java 11 and Docker (collections)
- Docker (for Elasticsearch via docker-compose)

### Start Elasticsearch locally
Copy `.env.template` to `.env` and populate paths:
```bash
cp .env.template .env
# Edit .env to set ES_DATA_PATH, ES_BACKUPS_PATH, ES_PLUGINS_PATH
docker-compose up -d
```
Elasticsearch will be available at http://localhost:9200.

### Full local stack
1. Start Elasticsearch (above)
2. Start Collections API: `./gradlew :collections-api:run` from `collections/`
3. Start GraphQL server: `yarn start:dev` from `server/`
4. Start Frontend: `npm start` from `frontend/`

---

## CI / CD

### GitHub Actions
- **File:** `.github/workflows/moneycol-server-ci.yml`
- **Trigger:** Pull requests that modify files matching `server/**/**.ts`
- **Steps:** Install (Node 16, Yarn cache), `yarn build`, run tests for `graphql`, `auth`, `users` packages
- **Secret required:** `FIREBASE_API_KEY_TEST`

### Cloud Build (GCP)
Each component has a `cloudbuild.yaml` for GCP Cloud Build:
- `server/cloudbuild.yaml` — Builds Docker image, pushes to `eu.gcr.io/$PROJECT_ID/moneycolserver`
- `frontend/cloudbuild.yaml` — Initializes git submodule via SSH key from Secret Manager, builds image
- `collections/cloudbuild.yaml` — Gradle + Jib, uploads test reports to GCS (`moneycol-ci`)

---

## Testing

### Server (Jest)
```bash
cd server && yarn test          # All packages
cd server/packages/graphql && yarn test
cd server/packages/auth && yarn test
cd server/packages/users && yarn test
```

### Frontend (Jest + React Testing Library)
```bash
cd frontend && npm test
```

### Collections (JUnit + TestContainers)
```bash
cd collections && ./gradlew test
# Download test reports from GCS:
gsutil cp -r gs://moneycol-ci/collections-test-reports .
```

### Infrastructure (Jest)
```bash
cd infra/gke-resizer/code && npm test
```

---

## Key Conventions

### TypeScript (Server & Frontend)
- Linting via TSLint (`tslint.json` at root)
- Strict TypeScript enabled in server packages
- GraphQL schema files live in `server/packages/graphql/schema/` with `.graphql` extension
- Schema is copied to `dist/schema/` during build — always run `yarn build` before starting

### Java (Collections)
- Java 11, source/target compatibility set in `build.gradle`
- Lombok used for boilerplate reduction — ensure annotation processing is enabled in your IDE
- JaCoCo for test coverage reports in `build/reports/`

### Git
- Branch naming: feature branches use `claude/<description>` format for AI-assisted work
- Conventional commit messages preferred
- `.gitignore` excludes `.env`, `node_modules`, and build artifacts
- Frontend uses a git submodule (`crema_template-2.0`) — must be initialized separately

### Environment
- Never commit `.env` files — use `.env.template` as a reference
- Firebase credentials should be provided via environment variables or GCP Secret Manager
- GCP service account credentials injected at runtime (not baked into images)

---

## Cloud Infrastructure (GCP)

| Resource | Purpose |
|---|---|
| GKE (`cluster-dev2`) | Runs all services in Kubernetes |
| Firestore | Primary database for users and collections |
| Elasticsearch 6.5 | Full-text search for banknote catalog |
| GCS (`moneycol-import`) | Raw data import bucket |
| GCS (`moneycol-ci`) | CI artifacts and test reports |
| Cloud Build | Automated Docker image builds |
| Cloud Scheduler | Periodic GKE cluster resizing |
| Cloud Functions | `gke-resizer` to scale cluster up/down |
| Secret Manager | GitHub SSH keys, Firebase credentials |
| GCR (`eu.gcr.io`) | Docker image registry |

---

## GraphQL API Examples

**Search banknotes:**
```graphql
query searchBanknotes($searchTerm: String!) {
  search(term: $searchTerm) {
    results { country banknoteName year description catalogCode }
    total
  }
}
# Variables: { "searchTerm": "ireland" }
```

**List collections:**
```graphql
query collectionsForCollector($collectorId: String!) {
  collections(collectorId: $collectorId) { name collectionId }
}
```

**Add collection:**
```graphql
mutation AddCollection($collection: NewCollectionInput!) {
  addCollection(collection: $collection) { name description }
}
```

---

## Common Pitfalls

1. **GraphQL server won't start** — Run `yarn build` first in `server/` to generate `dist/schema/`.
2. **Frontend build fails** — Ensure git submodule is initialized: `git submodule update --init --recursive`.
3. **Collections tests fail** — Docker must be running (TestContainers requirement). Set `FIREBASE_API_KEY` and `GOOGLE_APPLICATION_CREDENTIALS`.
4. **Elasticsearch not available** — Copy `.env.template` to `.env`, set data paths, run `docker-compose up -d`.
5. **Firestore permission denied in GKE** — The GKE default service account has insufficient scopes; a dedicated service account with Firestore access must be configured.
6. **Hot reload not reflecting changes** — In addition to `yarn start:dev`, run `yarn compile:watch` in each changed package (`auth`, `users`).
