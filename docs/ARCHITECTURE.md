# SizeOK: Backend Requirements & Technical Design

## Overview

SizeOK is a modular monolith backend in Java 21 + Spring Boot 3.3 with PostgreSQL, Flyway migrations, OpenAPI 3.1 contracts, and a rule-based, explainable Sizing & Recommendation Engine.

### Locked Architectural Decisions
1. **Measurement Profile Structure**: Multiple profiles per user account (`SELF`, `FAMILY`, etc.) with regional and unit preferences (`CM`, `IN`).
2. **Reference Sizing**: Supports both `Brand + Category + Size` and `Brand + Category + Size + Specific Product`. Reference sizes infer dimension ranges without overwriting actual measured body dimensions.
3. **Launch Market**: India-first (`IN`) with global-ready regional sizing (`IN`, `EU`, `US`, `UK`).
4. **Product Data Extraction**: Automated extraction with SSRF protection + manual size chart upload and admin review fallback (`EXTRACTED -> PENDING_REVIEW -> VERIFIED -> ACTIVE`).

### Core Modules (`com.sizeok`)
- `common`: Standardized exceptions, unit conversion (`CM` <-> `IN`), security/JWT & object-level authorization, audit logging.
- `identity`: Registration, login, token refresh, account management, privacy/consent, data export, and account deletion workflow.
- `profile`: Multi-profile CRUD per user account.
- `measurement`: Immutable versioned measurement sets, normalized centimetre storage with provenance (`MEASURED`, `MANUAL`, `IMPORTED`), and reference brand size inference & conflict detection.
- `catalog`: Master brands, brand aliases, hierarchical category taxonomy, category scoring weights & ease models, products, and variants.
- `sizechart`: Body (`BODY`) and garment (`GARMENT`) size charts, precedence resolution (`Product-specific > Brand+Category+Region > Brand+Category`), and admin verification lifecycle.
- `recommendation`: 4-stage Recommendation Engine (Normalization -> Chart Interpretation & Garment Ease -> Weighted Candidate Scoring -> Confidence & Explanation) with immutable snapshots.
- `ingestion`: Product URL submission (`202 Accepted`), SSRF protection, retailer parsing, and manual size-chart fallback (`NEEDS_USER_INPUT`).
- `feedback`: Fit feedback capture linked to exact recommendation, profile measurement version, and size chart version.
- `admin`: Catalog administration, chart verification workflow, and recommendation audit inspection.
