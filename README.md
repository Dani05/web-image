# 🖼️ Web Image

A full-stack web application for uploading, viewing, and managing images. Built with a **Spring Boot** backend, a **React** frontend, backed by **PostgreSQL**, and fully containerized with **Docker** and deployable to **OpenShift**.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run Locally with Docker Compose](#run-locally-with-docker-compose)
  - [Environment Variables](#environment-variables)
- [Deployment](#deployment)
  - [OpenShift](#openshift)
  - [CI/CD with GitHub Actions](#cicd-with-github-actions)
- [Load testing & autoscaling with Locust on OpenShift](#load-testing--autoscaling-with-locust-on-openshift)
- [Architecture](#architecture)

---

## Overview

**Web Image** is a full-stack image management platform. Users can register an account, log in, upload images with a name and description, browse all uploaded images, and delete their own images. Authentication is handled via JWT tokens.

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 21, Spring Boot 4, Spring Security, Spring Data JPA, Lombok |
| Frontend   | React 19, React Router DOM 7, CSS   |
| Database   | PostgreSQL 15/16                    |
| Auth       | JWT (Nimbus JOSE + JWT)             |
| Container  | Docker, Docker Compose              |
| Web Server | Nginx (unprivileged)                |
| CI/CD      | GitHub Actions                      |
| Deploy     | OpenShift                           |

---

## Project Structure

```
web-image/
├── web-image-backend/          # Spring Boot REST API (Java)
│   └── src/main/java/
│       └── org/example/webimagebackend/
│           ├── controller/     # REST controllers (ImageController, ProfileController)
│           ├── service/        # Business logic (ImageService, ProfileService, TokenService)
│           ├── persistence/    # JPA entities and repositories
│           ├── model/          # Domain models
│           └── config/         # Security configuration
├── web-image-frontend/         # React SPA (JavaScript)
│   └── src/
│       ├── services/api.js     # API client (fetch wrapper)
│       └── ...                 # Components, pages, styles
├── openshift/                  # OpenShift Kubernetes manifests
│   ├── postgresql.yaml         # PostgreSQL Deployment + Service + PVC
│   └── uploads-pvc.yaml        # PersistentVolumeClaim for image uploads
├── .github/workflows/          # GitHub Actions CI/CD pipelines
│   ├── openshift.yml           # Deploy to OpenShift
│   └── fured_workflow.yml      # Additional workflow
├── Dockerfile                  # Multi-stage Docker build (backend + frontend)
├── docker-compose.yml          # Production-like compose setup
├── docker-compose-local.yml    # Local development (PostgreSQL only)
└── nginx.conf                  # Nginx configuration for the frontend
```

---

## Features

- 🔐 **User Authentication** – Register and login with username/password; receives a signed JWT token (HS256, 24h expiry)
- 📤 **Image Upload** – Authenticated users can upload images with a name (max 40 chars) and optional description
- 🖼️ **Image Gallery** – Browse all uploaded images (public)
- 🔍 **Image Detail** – View a single image by ID
- 🗑️ **Image Deletion** – Authenticated users can delete images
- 💾 **Persistent Storage** – Images stored on disk; metadata stored in PostgreSQL

---

## API Endpoints

### Authentication — `/api/profile`

| Method | Endpoint            | Auth     | Description              |
|--------|---------------------|----------|--------------------------|
| POST   | `/api/profile/register` | ❌ No | Register a new user      |
| POST   | `/api/profile/login`    | ❌ No | Login and receive a JWT  |

### Images — `/api/images`

| Method | Endpoint          | Auth       | Description                    |
|--------|-------------------|------------|--------------------------------|
| GET    | `/api/images`     | ❌ No      | Get all images                 |
| GET    | `/api/images/{id}`| ❌ No      | Get a single image by ID       |
| POST   | `/api/images`     | ✅ Bearer  | Upload a new image (multipart) |
| PUT    | `/api/images/{id}`| ✅ Bearer  | Update image metadata          |
| DELETE | `/api/images/{id}`| ✅ Bearer  | Delete an image                |

> **POST `/api/images`** expects `multipart/form-data` with fields: `file`, `name`, `description`.

---

## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- Java 21 (for running the backend locally without Docker)
- Node.js 20 (for running the frontend locally without Docker)

### Run Locally with Docker Compose

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Dani05/web-image.git
   cd web-image
   ```

2. **Start the PostgreSQL database** (local development):

   ```bash
   docker compose -f docker-compose-local.yml up -d
   ```

3. **Create a `.env` file** in the project root (see [Environment Variables](#environment-variables)).

4. **Build and start the full application:**

   ```bash
   docker compose up --build
   ```

5. **Access the application:**
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - Backend API: [http://localhost:8080](http://localhost:8080)

---

### Environment Variables

Create a `.env` file in the project root:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/webimage
SPRING_DATASOURCE_USERNAME=webimage
SPRING_DATASOURCE_PASSWORD=webimage

# JWT Secret (min 32 chars recommended)
APP_SECURITY_JWT_SECRET_KEY=your-very-secret-key-here

# Frontend API URL (used at build time)
REACT_APP_API_BASE_URL=http://localhost:8080
```

> ⚠️ Never commit your `.env` file or real secrets to version control.

---

## Deployment

### OpenShift

Kubernetes/OpenShift manifests are located in the `openshift/` directory.

```bash
# Apply PostgreSQL deployment, service, and PVC
oc apply -f openshift/postgresql.yaml

# Apply the uploads PersistentVolumeClaim
oc apply -f openshift/uploads-pvc.yaml
```

### CI/CD with GitHub Actions

Two GitHub Actions workflows are included:

| Workflow | File | Description |
|----------|------|-------------|
| Fured OpenShift Deploy | `.github/workflows/fured_workflow.yml` | Builds and deploys the application to OpenShift |
| OpenShift Deploy | `.github/workflows/openshift.yml` | Additional CI/CD steps  |

## Load testing & autoscaling with Locust on OpenShift

To verify the application under load and observe Horizontal Pod Autoscaling (HPA) behaviour, the repository includes a Locust-based load test and OpenShift manifests.

#### Components

- `loadtest/` – Locust load test image
  - `Dockerfile` – container image for Locust
  - `locustfile.py` – test scenarios (browse, upload, update, delete)
  - `requirements.txt` – pinned Locust & Python deps
- `openshift/loadtest-configmap.yaml` – runtime configuration for Locust
  - `TARGET_HOST` – backend URL in OpenShift (Route of the Spring Boot API)
  - `LOCUST_USERS`, `LOCUST_SPAWN_RATE`, `LOCUST_RUN_TIME` – load profile
  - `WEIGHT_*`, `WAIT_*` – request mix and think time
- `openshift/loadtest-job.yaml` – headless Locust Job for scripted runs
- `openshift/loadtest-ui.yaml` – Locust UI Deployment + Service + Route
- `openshift/web-image-backend-hpa.yaml` – HPA definition for the backend
- `.github/workflows/locust-loadtest.yml` – GitHub Actions workflow that builds the Locust image, deploys it to OpenShift and runs the headless job

#### 1. Configure OpenShift & registry access

1. Make sure your backend is deployed to OpenShift and exposed via a Route (see the main OpenShift workflow in `.github/workflows/openshift.yml`).
2. Edit `openshift/loadtest-configmap.yaml` and set:
   - `TARGET_HOST` to your backend Route, e.g. `https://web-image-backend-<ns>.apps.<cluster-domain>`
   - Optionally tune `LOCUST_USERS`, `LOCUST_SPAWN_RATE`, `LOCUST_RUN_TIME` for desired intensity.
3. Configure GitHub Secrets in your repository (Settings → Secrets and variables → Actions):
   - `OPENSHIFT_SERVER` – OpenShift API URL
   - `OPENSHIFT_TOKEN` – token with access to your project (e.g. `oc whoami -t`)
   - `OPENSHIFT_NAMESPACE` – target OpenShift project/namespace
   - `IMAGE_REGISTRY` – e.g. `ghcr.io`
   - `IMAGE_NAME` – e.g. `<your-gh-user-or-org>/web-image-locust`
   - `REGISTRY_USERNAME` / `REGISTRY_PASSWORD` – credentials for the registry

#### 2. Run the Locust load test from GitHub Actions

The workflow is defined in `.github/workflows/locust-loadtest.yml` and is **manual only** (`workflow_dispatch`). It will:

1. Build the Locust image from `loadtest/` and push it to `${IMAGE_REGISTRY}/${IMAGE_NAME}:latest`.
2. Install the `oc` CLI and log in to OpenShift using the provided token.
3. Patch `openshift/loadtest-job.yaml` and `openshift/loadtest-ui.yaml` to point to the freshly built image.
4. Apply `loadtest-configmap.yaml`, `loadtest-ui.yaml` and `loadtest-job.yaml`.
5. Start the headless Job, wait for completion and print Locust statistics to the job log.

To run it:

1. Open your GitHub repository.
2. Go to **Actions → Run Locust load test on OpenShift**.
3. Click **Run workflow** and wait for it to finish.
4. Inspect the last step ("Show Job logs") for RPS, response times and failures.

#### 3. Using the Locust UI in the cloud

Besides the headless job, you can use the Locust web UI deployed in the university OpenShift cluster.

1. Ensure `openshift/loadtest-ui.yaml` has been applied (either via the workflow or manually):

```bash
oc apply -f openshift/loadtest-configmap.yaml
oc apply -f openshift/loadtest-ui.yaml
```

2. Get the externally accessible Route for the UI:

```bash
oc project <your-namespace>
oc get route locust-ui
```

3. Copy the `HOST/PORT` value, e.g. `https://locust-ui-<ns>.apps.<cluster-domain>` and open it in your browser.
4. On the Locust start page you can configure number of users and spawn rate, then click **Start swarming** to generate traffic towards `TARGET_HOST`.

#### 4. Observing autoscaling

To test autoscaling behaviour of the backend under load:

1. Apply the HPA definition:

```bash
oc apply -f openshift/web-image-backend-hpa.yaml
```

2. Run a sustained load test (e.g. `LOCUST_USERS=80`, `LOCUST_RUN_TIME=20m`).
3. Watch the HPA and backend pods:

```bash
oc get hpa
oc describe hpa web-image-backend-hpa
oc get pods -l app=web-image-backend -w
oc top pods -l app=web-image-backend
```

4. Verify that pods scale up under load and scale down again after the test.

5. When finished, you can clean up load test resources:

```bash
oc delete job locust-loadtest-headless --ignore-not-found
oc delete deployment locust-ui --ignore-not-found
oc delete svc locust-ui --ignore-not-found
oc delete route locust-ui --ignore-not-found
```

6. Example for events from the HPA:

```
  Normal   SuccessfulRescale             46m (x2 over 52m)     horizontal-pod-autoscaler  New size: 4; reason: cpu resource above target
  Normal   SuccessfulRescale             41m                   horizontal-pod-autoscaler  New size: 2; reason: All metrics below target
  Normal   SuccessfulRescale             40m (x2 over 3h17m)   horizontal-pod-autoscaler  New size: 1; reason: All metrics below target
```

---

## Architecture

```
┌──────────────┐       HTTP/REST        ┌───────────────────┐
│              │ ────────���───────►  │                   │
│  React SPA   │                        │  Spring Boot API  │
│  (Nginx:8080)│ ◄───────────────────── │  (Java 21, :8080) │
│              │       JSON / JWT       │                   │
└──────────────┘                        └────────┬──────────┘
                                                 │ JPA
                                        ┌────────▼──────────┐
                                        │                   │
                                        │    PostgreSQL     │
                                        │    (port 5432)    │
                                        │                   │
                                        └───────────────────┘
```

---

## Author

**Dani05** — [@Dani05](https://github.com/Dani05)
