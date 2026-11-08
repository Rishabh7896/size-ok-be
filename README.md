# SizeOK Backend (`size-ok-be`)

SizeOK is a sizing intelligence and garment size recommendation backend built as a **Java 21 + Spring Boot 3.3 modular monolith** with PostgreSQL, Flyway migrations, OpenAPI 3.1 contracts, and a Python worker scaffold for product/size-chart extraction.

---

## Core Product & Engineering Principles

1. **Strict Separation of Measurement Domains**
   - **Body Measurements**: The user's actual physical dimensions (normalized internally to centimetres `CM`, with provenance `MEASURED`, `MANUAL`, or `IMPORTED`).
   - **Garment Measurements**: Physical dimensions of a finished clothing item (`measurement_basis = GARMENT`), evaluated using category-specific and fit-preference-specific ease models (`SLIM`, `REGULAR`, `RELAXED`, `OVERSIZED`) and fabric stretch factors.
   - **Brand Size Recommendations**: Measurement ranges that a brand associates with a size label (`measurement_basis = BODY`).
   - *Rule*: Measurements inferred from a reference brand size are stored in `reference_sizes` and are **never** written into `measurement_values` as actual measured body dimensions.

2. **Category-Scoped Reference Sizing**
   - A user's size `M` in a Zara Men's T-Shirt is never assumed to apply to jeans or formal shirts. Every reference size is scoped to `(brand_id, category_id, region, fit_preference, optional product_id)`.
   - Multiple reference sizes within the same category are combined with automatic conflict detection when dimension ranges are disjoint.

3. **Immutable Versioning & Reproducible Recommendations**
   - Updating a user's measurements creates a new immutable `measurement_sets` version (`version + 1`) and marks the prior version `SUPERSEDED`.
   - Size charts follow a strict verification lifecycle (`EXTRACTED -> PENDING_REVIEW -> VERIFIED -> ACTIVE`, plus `SUPERSEDED`, `REJECTED`, `EXPIRED`). Unverified charts never silently become brand defaults.
   - Every recommendation stores the exact `measurement_set_id`, `measurement_set_version`, `size_chart_id`, `chart_version`, and measurement snapshot JSON so historical recommendations remain 100% reproducible even after brand charts or user measurements change.

---

## Architecture & Module Layout

```text
size-ok-be/
├── docs/
│   ├── ARCHITECTURE.md               # Engineering specification & architectural decisions
│   └── openapi.yaml                  # OpenAPI 3.1 REST contract
├── src/main/java/com/sizeok/
│   ├── common/                       # Exceptions, UnitConverter (CM/IN), JWT Security, AuditService
│   ├── identity/                     # Auth (/auth/register, /auth/login) & User/Privacy/Export/Deletion
│   ├── profile/                      # Multi-profile management per user account with BOLA protection
│   ├── measurement/                  # Versioned measurement sets & category-scoped reference sizes
│   ├── catalog/                      # Master brands, aliases, hierarchical categories, products & variants
│   ├── sizechart/                    # BODY & GARMENT size charts, precedence resolution & versioning
│   ├── recommendation/               # 4-stage Recommendation Engine & candidate scoring
│   ├── ingestion/                    # SSRF-protected product URL ingestion & manual chart fallback
│   ├── feedback/                     # Post-purchase/try-on fit feedback linked to snapshots
│   └── admin/                        # Catalog administration, chart verification & recommendation audit
├── src/main/resources/
│   ├── application.yml               # Spring Boot configuration
│   └── db/migration/                 # Flyway PostgreSQL DDL & seed migrations (V1..V5)
├── python-workers/                   # Python extraction & chart normalization worker modules
├── docker-compose.yml                # Local PostgreSQL 16 & Redis 7 services
└── pom.xml                           # Maven build configuration
```

---

## Technology Stack

| Layer | Technology |
| :--- | :--- |
| Language & Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 (Web, Data JPA, Validation, Security, Actuator) |
| Database & Migrations | PostgreSQL 16 + Flyway (`V1`–`V5`), H2 (PostgreSQL mode for tests) |
| Cache | Redis 7 |
| Authentication | Stateless JWT Bearer Tokens + Role & Object-Level Authorization |
| API Documentation | SpringDoc OpenAPI 3.1 (`/swagger-ui.html`, `/api-docs`, `docs/openapi.yaml`) |
| Extraction Workers | Python 3 (`python-workers/`) |

---

## Quick Start

### 1. Start PostgreSQL and Redis
```bash
docker compose up -d
```

### 2. Build and Run Tests
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn test
```

### 3. Run the Application Locally
```bash
mvn spring-boot:run
```
- **Base API URL**: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI 3.1 JSON**: `http://localhost:8080/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

---

## Core API Endpoints (`/api/v1`)

| Domain | Method & Endpoint | Description |
| :--- | :--- | :--- |
| **Auth** | `POST /auth/register` | Register account and create primary profile |
| **Auth** | `POST /auth/login` | Authenticate and receive JWT access/refresh tokens |
| **User & Privacy** | `GET, PATCH, DELETE /users/me` | Get, update, or initiate account deletion |
| **User & Privacy** | `GET, PATCH /users/me/privacy` | View or update consent preferences |
| **User & Privacy** | `GET /users/me/export` | Export user account and profile data |
| **Profiles** | `POST, GET /profiles` | Create or list measurement profiles (self, family) |
| **Profiles** | `GET, PATCH /profiles/{id}` | Retrieve or update profile (object-level authorized) |
| **Measurements** | `POST, GET /profiles/{id}/measurement-sets` | Save new measurement version or list version history |
| **Measurements** | `GET, PATCH /measurement-sets/{id}` | Inspect version or patch measurements (creates `v+1`) |
| **Reference Sizes** | `POST, GET /profiles/{id}/reference-sizes` | Add or list category-scoped reference brand sizes |
| **Reference Sizes** | `DELETE /reference-sizes/{id}` | Delete a reference size |
| **Catalog** | `GET /brands`, `GET /brands/{id}` | Search brands and view brand details |
| **Catalog** | `GET /brands/{id}/size-charts` | List size charts for a brand |
| **Catalog** | `GET /categories` | Retrieve hierarchical category tree |
| **Catalog** | `GET /products`, `GET /products/{id}` | Search products and inspect variants |
| **Catalog** | `GET /products/{id}/size-charts` | Get resolved size charts for a product |
| **URL Ingestion** | `POST /product-ingestions` | Submit product URL (`202 Accepted`, SSRF-guarded) |
| **URL Ingestion** | `GET /product-ingestions/{id}` | Poll ingestion job status |
| **URL Ingestion** | `POST /product-ingestions/{id}/size-chart` | Upload manual size chart fallback |
| **URL Ingestion** | `POST /product-ingestions/{id}/confirm-chart` | Confirm chart and trigger recommendation |
| **Recommendations** | `POST /recommendations` | Generate size recommendation for known product |
| **Recommendations** | `POST /recommendations/by-url` | Generate recommendation directly from URL |
| **Recommendations** | `GET /recommendations/{id}` | Retrieve reproducible recommendation snapshot |
| **Recommendations** | `GET /profiles/{id}/recommendations` | List recommendation history for a profile |
| **Feedback** | `POST /recommendations/{id}/feedback` | Submit post-purchase/try-on fit feedback |
| **Admin** | `POST /admin/brands`, `POST /admin/products` | Create master brands and products |
| **Admin** | `POST /admin/size-charts` | Create brand or product size chart |
| **Admin** | `PATCH /admin/size-charts/{id}/status` | Verify, activate, or reject a size chart |
| **Admin** | `GET /admin/recommendations/{id}/audit` | Inspect full recommendation snapshot, chart, & feedback |

For detailed development guidelines, database schema conventions, and testing workflows, see [DEVELOPING.md](./DEVELOPING.md).

