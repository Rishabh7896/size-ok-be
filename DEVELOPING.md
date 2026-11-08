# Developing SizeOK Backend (`size-ok-be`)

This guide covers setting up your local environment, working with database migrations, understanding the internal module boundaries, running the test suite, and extending the SizeOK backend.

---

## 1. Prerequisites & Environment Setup

- **JDK 21** (OpenJDK 21)
- **Apache Maven 3.9+**
- **Docker & Docker Compose** (for running local PostgreSQL 16 and Redis 7)
- **Python 3.11+** (optional, for working on `python-workers/`)

On macOS with Homebrew:
```bash
brew install openjdk@21 maven
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
```

Verify your toolchain:
```bash
java -version   # Should report OpenJDK 21.x
mvn -version    # Should report Apache Maven using Java 21
```

---

## 2. Database & Flyway Migrations

SizeOK uses **PostgreSQL** as the primary relational store and **Flyway** (`src/main/resources/db/migration/`) for schema management. Hibernate is configured with `ddl-auto: validate` so the JPA entities must strictly match the Flyway schema.

### Migration Files
- `V1__init_identity_and_profile_schema.sql`: `users`, `user_sessions`, `consent_records`, `profiles`, `measurement_sets`, `measurement_values`, `audit_events`.
- `V2__init_catalog_and_recommendation_schema.sql`: `brands`, `brand_aliases`, `categories`, `category_dimension_weights`, `products`, `product_variants`, `size_charts`, `size_options`, `size_chart_measurements`, `reference_sizes`, `product_ingestions`, `recommendations`, `recommendation_candidates`, `fit_feedback`.
- `V3__seed_categories_and_brands.sql`: Hierarchical categories (`APPAREL -> TOPS / BOTTOMS / ONE_PIECE / INNERWEAR`), category dimension weights, garment ease models, and verified brands (`Zara`, `H&M`, `Nike`, `Levi's`).
- `V4__seed_body_size_charts.sql`: Verified `BODY` measurement size charts (Zara Men's Tops, Zara Men's Jeans, Nike Men's Tops).
- `V5__seed_garment_charts_and_products.sql`: Verified `GARMENT` measurement size chart (H&M Men's Shirts) and sample products (`prod_5001`, `prod_5002`, `prod_5003`).

### Adding a New Migration
1. Create `src/main/resources/db/migration/V<next>__<descriptive_name>.sql`.
2. Never hardcode measurement dimensions (`chest`, `waist`, etc.) as table columns—store dimensions as rows in `measurement_values`, `size_chart_measurements`, and `category_dimension_weights`.
3. Ensure SQL syntax is standard PostgreSQL and compatible with H2's `MODE=PostgreSQL` used in `application-test.yml`.

---

## 3. Module Architecture & Responsibilities

Keep domain logic inside each module's service/engine layer rather than controllers.

### `com.sizeok.common`
- `UnitConverter`: Normalizes units (`CM`, `IN`), converts inches to centimetres (`1 IN = 2.54 CM`), aliases common dimension names (`BUST -> CHEST`, `HIPS -> HIP`, `SLEEVE_LENGTH -> SLEEVE`), and validates physical ranges.
- `JwtTokenService` & `JwtAuthenticationFilter`: Stateless Bearer JWT authentication.
- `CurrentUserProvider`: Enforces authentication and role checks (`SUPER_ADMIN`, `CATALOG_ADMIN`, `DATA_REVIEWER`, `SUPPORT`).
- `AuditService`: Records immutable audit trail events in `audit_events`.

### `com.sizeok.profile` & `com.sizeok.measurement`
- **Object-Level Authorization (BOLA)**: Always call `profileService.requireAccessibleProfile(profileId)` before reading or mutating any profile, measurement set, reference size, or recommendation.
- **Measurement Immutability**: `POST /profiles/{id}/measurement-sets` and `PATCH /measurement-sets/{id}` never overwrite existing rows in `measurement_sets`; they mark the previous active set `SUPERSEDED` and insert a new version (`version + 1`) with `status = ACTIVE`.
- **Reference Size Isolation**: `MeasurementService.normalizeBodyMeasurementSource` rejects `REFERENCE_INFERRED` on body measurement sets. Reference brand sizes are stored in `reference_sizes` with their resolved chart ranges (`inferred_ranges_json`).

### `com.sizeok.sizechart`
- **Precedence Resolution** (`SizeChartService.resolveApplicableChart`):
  1. Verified/Active Product-specific chart (`product_id = X`)
  2. Verified/Active Brand + Category + Region chart (`brand_id = B`, `category_id = C`, `region = R`, `product_id IS NULL`)
  3. Verified/Active Brand + Category chart (`brand_id = B`, `category_id = C`, `product_id IS NULL`)
- **Verification Lifecycle**: Only `ACTIVE` and `VERIFIED` charts are returned by authoritative resolution. Activating a new brand-level chart automatically marks previous active charts for the same `(brand_id, category_id, region)` as `SUPERSEDED`.

### `com.sizeok.recommendation`
`RecommendationEngine` executes a 4-stage pipeline:
1. **Stage 1 (Normalization & Evidence)**: Loads active `measurement_sets` (in `CM`) and category-matching `reference_sizes`. Detects disjoint reference range conflicts.
2. **Stage 2 (Chart Interpretation & Ease Model)**:
   - `BODY` charts compare user measurements directly against `[min_value_cm, max_value_cm]`.
   - `GARMENT` charts subtract required garment ease (`default_ease_slim_cm`, `default_ease_regular_cm`, `default_ease_relaxed_cm`, `default_ease_oversized_cm` scaled by product `stretch_level`) from garment dimensions.
3. **Stage 3 (Weighted Scoring)**: Computes $Score(s) = 100 - \sum w_d P_d(s)$ using `category_dimension_weights`, penalizing tight mismatches more heavily than loose mismatches.
4. **Stage 4 (Confidence & Snapshot Persistence)**: Computes `HIGH`, `MEDIUM`, or `LOW` confidence independently of `fitScore` and persists an immutable snapshot (`measurement_set_id`, `measurement_set_version`, `size_chart_id`, `chart_version`, `snapshot_measurements_json`).

### `com.sizeok.ingestion`
- `SsrfUrlValidator` validates all user-submitted URLs before processing, blocking non-HTTP(S) schemes, `localhost`, loopback (`127.0.0.1`, `::1`), RFC1918 private networks, link-local, and cloud metadata endpoints (`169.254.169.254`, `metadata.google.internal`).
- Supports `idempotencyKey` to prevent duplicate product/chart creation on job retries.
- Returns `NEEDS_USER_INPUT` (`requiredAction: UPLOAD_SIZE_CHART`) when a retailer URL is unsupported or has no extractable chart.

---

## 4. Running Tests & Verification

Run the full integration and acceptance test suite:
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn test -B
```

### What the Acceptance Test Suite Verifies
- **`SizeOkAcceptanceIntegrationTest`**:
  - User registration and default profile creation.
  - Saving body measurements (version 1) and patching measurements to create immutable version 2 while superseding version 1.
  - Adding category-scoped reference brand sizes and detecting multi-reference conflicts.
  - Generating recommendations on `BODY` charts (`prod_5001`) and `GARMENT` charts (`prod_5002` with garment ease).
  - Submitting post-purchase fit feedback linked to the exact recommendation, profile version, and chart version.
- **`SizeOkIngestionSecurityAndReproducibilityTest`**:
  - Unit conversion (`IN` -> `CM`) and confidence reduction (`HIGH` -> `MEDIUM`) when primary measurements are missing.
  - Supported retailer URL ingestion (`COMPLETED`) and duplicate job idempotency.
  - Unsupported retailer URL fallback (`NEEDS_USER_INPUT` / `UPLOAD_SIZE_CHART`).
  - SSRF blocking on cloud metadata/private endpoints (`400 Bad Request` / `SSRF_BLOCKED_HOST`).
  - Cross-user profile access denial (`403 Forbidden`).
  - Adding a new brand/chart/product via Admin APIs without an app release, superseding chart v1 with v2, and verifying historical recommendation reproducibility (`chartVersion = 1`) and `/api/v1/admin/recommendations/{id}/audit`.
  - Account deletion and personal data anonymization (`DELETION_PENDING`).

