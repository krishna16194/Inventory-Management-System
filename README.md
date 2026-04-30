# Inventory Management System

A full-stack **Spring Boot** web application for managing product inventory, creating bills, and tracking sales — with multi-user login so each user has their own isolated data.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Multi-user login** | Register / login; each user sees only their own products and bills |
| 📦 **Product management** | Add, edit, soft-delete products; Excel import & export |
| 🧾 **Billing** | Barcode scanner + manual search; cart management; printable receipts |
| 📊 **Statistics dashboard** | Revenue, bill count, top-selling products, low-stock alerts |
| 📋 **Bill history** | Filter by date range or customer name |
| 📷 **Barcode scanner** | Uses native `BarcodeDetector` API with dim-light canvas brightness boost; torch button for dark environments |
| 🌐 **Internationalisation** | English and Tamil (`?lang=en` / `?lang=ta`) |
| 🐘 **PostgreSQL** | Auto-started via Docker Compose for local dev; Render-hosted in production |

---

## ⚡ Quick Start

### Option A — PostgreSQL via Docker (recommended)

**Requirement:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

Spring Boot auto-starts the PostgreSQL container — no manual database setup needed.

**IntelliJ IDEA:**
1. Make sure Docker Desktop is running
2. Add VM option: `-Dspring.profiles.active=postgres`
3. Press **▶ Run**

**Terminal:**
```cmd
gradlew bootRun --args="--spring.profiles.active=postgres"
```

**First-time container warm-up (optional):**
```powershell
.\setup-postgres.ps1
```

---

### Option B — H2 Embedded Database (zero setup)

No Docker required. Data is stored in `inventory-db.mv.db` in the project root.

**IntelliJ IDEA:** Run with no extra VM options (default profile).

**Terminal:**
```cmd
gradlew bootRun
```

H2 console available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./inventory-db`
- Username: `sa` | Password: *(empty)*

---

### First use

1. Open `http://localhost:8080/register` and create an account
2. Log in at `http://localhost:8080/login`
3. You are redirected to the **Stats Dashboard**

---

## 🏗 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 (BCrypt, form login) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database (local H2) | H2 2.2 (file-based) |
| Database (prod / Docker) | PostgreSQL 16 |
| Templating | Thymeleaf 3 + Bootstrap 5.3 |
| Excel | Apache POI 5.2 |
| Build | Gradle 8 (wrapper included) |
| Container | Docker / Docker Compose |

---

## 📁 Project Structure

```
src/main/java/com/kr/bill/inventorymangementsystem/
├── config/
│   ├── SecurityConfig.java            # Form login, BCrypt, logout
│   ├── GlobalModelAdvice.java         # Injects username/userInitial into all views
│   ├── I18nConfig.java                # Language switcher (en / ta)
│   ├── DataSourceConfig.java          # Placeholder; real config via EnvironmentPostProcessor
│   └── DatabaseUrlEnvironmentPostProcessor.java  # Parses Render DATABASE_URL → JDBC props
├── controller/
│   ├── AuthController.java            # /login, /register
│   ├── DashboardController.java       # /dashboard
│   ├── BillingController.java         # /billing/**
│   ├── BillHistoryController.java     # /bill-history
│   ├── ProductController.java         # /products/**
│   ├── StatsController.java           # /stats-dashboard
│   └── InventoryApiController.java    # /api/inventory/** (search, import, export)
├── model/
│   ├── AppUser.java                   # User entity
│   ├── Product.java                   # Product entity (owner-scoped, soft-delete)
│   ├── Bill.java                      # Bill entity
│   └── BillItem.java                  # Bill line item
├── repository/
│   ├── UserRepository.java
│   ├── ProductRepository.java         # Owner-scoped queries
│   └── BillRepository.java            # Owner-scoped queries
├── service/
│   ├── UserService.java               # Registration, UserDetailsService impl
│   ├── StatsService.java              # Revenue, bill counts, low-stock (owner-scoped)
│   └── ExcelService.java              # .xlsx import / export
└── dto/
    ├── DailySale.java
    └── ProductSale.java
```

---

## 🌐 Profiles

| Profile | Database | Activate via |
|---|---|---|
| *(default)* | H2 embedded file | No flag needed |
| `postgres` | PostgreSQL via Docker Compose | `-Dspring.profiles.active=postgres` |
| `render` | PostgreSQL from `DATABASE_URL` env var | Set automatically by `render.yaml` |

---

## 🚀 Deployment on Render

This project is pre-configured for one-click deployment on [Render](https://render.com) using the `render.yaml` blueprint.

1. Fork / push this repo to GitHub
2. On Render → **New → Blueprint** → connect your repo
3. Render will create:
   - A **Web Service** (Docker, free tier)
   - A **PostgreSQL** managed database (free tier)
4. The `DATABASE_URL` is injected automatically; `DatabaseUrlEnvironmentPostProcessor` converts it to JDBC properties at startup

No environment variables need to be set manually.

---

## 🔧 Development Commands

```cmd
# Run with H2 (default)
gradlew bootRun

# Run with PostgreSQL (Docker must be running)
gradlew bootRun --args="--spring.profiles.active=postgres"

# Build executable JAR
gradlew clean bootJar

# Run tests
gradlew test
```

---

## 🗂 API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/inventory` | List all products (owner-scoped) |
| `GET` | `/api/inventory/search?query=` | Search products by name or ID |
| `GET` | `/api/inventory/{id}` | Get single product by barcode ID |
| `GET` | `/api/inventory/low-stock` | Products at or below low-stock threshold |
| `GET` | `/api/inventory/export` | Download products as `.xlsx` |
| `POST` | `/api/inventory/import` | Upload products from `.xlsx` |
| `POST` | `/billing/add-scan` | Add product to cart (AJAX barcode scan) |

---

## 📸 Screenshots

| Stats Dashboard | Main Dashboard (Billing) | Bill History |
|---|---|---|
| Revenue KPIs, low-stock alerts, daily sales chart | Barcode scanner, cart, checkout | Date/customer filter, printable receipts |
