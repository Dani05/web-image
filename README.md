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
