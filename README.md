# SmartyCommerce — AI-Powered E-Commerce Platform

A full-stack e-commerce platform featuring a **Hybrid Recommendation Engine** that combines Content-Based Filtering (CBF) with Item-to-Item Collaborative Filtering (CF) to deliver personalized product recommendations.

Built as a **graduation project**, the system consists of four independently deployable services: a Spring Boot backend, a Python recommendation microservice, a Next.js frontend, and a verification service.

![Main Page](Demo%20Pictures/Main%20Page.png)

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Services](#services)
  - [Smarty Commerce (Java Backend)](#1-smarty-commerce--java-backend)
  - [Recommendation Service (Python)](#2-recommendation-service--python)
  - [Frontend (Next.js)](#3-frontend--nextjs)
  - [Verification Service](#4-verification-service)
- [Recommendation Algorithm](#recommendation-algorithm)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [AWS EC2 Deployment](#aws-ec2-deployment)
- [Environment Variables](#environment-variables)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Browser (Client)                          │
│                      http://hostname:3000                          │
└─────────────────┬───────────────────────────────────────────────────┘
                  │
                  │  All requests go to port 3000
                  │  (Next.js rewrites proxy API calls)
                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Frontend — Next.js (Port 3000)                   │
│                                                                     │
│  /api/core/*  ──proxy──►  Java Backend (Port 8080)                 │
│  /api/rec/*   ──proxy──►  Python Rec Service (Port 8000)           │
└─────────────────┬──────────────────────┬────────────────────────────┘
                  │                      │
         ┌────────▼────────┐    ┌────────▼────────┐
         │  Smarty Commerce │    │  Recommendation  │
         │  Spring Boot     │◄───│  Service (FastAPI)│
         │  (Port 8080)     │    │  (Port 8000)      │
         │                  │    │                    │
         │  • REST API      │    │  • Hybrid Engine   │
         │  • JWT Auth      │    │  • CF + CBF        │
         │  • RBAC          │    │  • Auto-rebuild    │
         │  • AWS SES Email │    │  • Cosine Sim.     │
         └────────┬─────────┘    └───────────────────┘
                  │
         ┌────────▼────────┐    ┌──────────────────────────┐
         │   PostgreSQL     │    │  Verification Service     │
         │   Database       │    │  (Same Tomcat — Port 8080)│
         └─────────────────┘    └──────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.5, Spring Security, Spring Data JPA, JWT (jjwt 0.12) |
| **Database** | PostgreSQL (production), H2 (development) |
| **Recommendation** | Python 3.11+, FastAPI, NumPy, scikit-learn, APScheduler |
| **Frontend** | Next.js 16, React 19, TypeScript, CSS Modules |
| **Email** | AWS SES (Amazon Simple Email Service) |
| **Deployment** | AWS EC2, Apache Tomcat, PM2 (for Python service) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |

---

## Project Structure

```
Graduation-Project/
├── smarty-commerce/              # Java Spring Boot backend (WAR)
│   └── smarty-commerce/
│       └── src/main/java/com/graduation/smarty_commerce/
│           ├── Security/         # JWT auth, filters, RBAC
│           ├── Service/          # Business logic
│           ├── io/
│           │   ├── Entity/       # JPA entities (13 entities)
│           │   └── Repository/   # Spring Data repositories
│           ├── shared/           # Utils, DTOs, AmazonSES
│           └── ui/
│               ├── Controller/   # REST controllers (9 controllers)
│               └── Model/        # Request/Response models
│
├── recommendation-service/       # Python FastAPI microservice
│   ├── main.py                   # App entry point + lifespan
│   ├── engine.py                 # Hybrid recommendation engine (CF+CBF)
│   ├── data_fetcher.py           # Fetches data from Java backend
│   ├── scheduler.py              # Periodic model rebuild (APScheduler)
│   ├── models.py                 # Pydantic data models
│   ├── config.py                 # Configuration & weights
│   └── routers/                  # FastAPI route handlers
│       ├── recommendations.py    # GET /recommendations/{userId}
│       └── admin.py              # POST /admin/rebuild
│
├── frontend/                     # Next.js 16 frontend
│   └── src/
│       ├── app/                  # App Router pages
│       │   ├── page.tsx          # Homepage (hero + recommendations)
│       │   ├── products/         # Product listing + detail
│       │   ├── cart/             # Shopping cart
│       │   ├── checkout/         # Checkout flow
│       │   ├── orders/           # Order history
│       │   ├── wishlist/         # Wishlist
│       │   ├── profile/          # User profile & addresses
│       │   ├── admin/            # Admin dashboard + CRUD
│       │   ├── login/            # Login page
│       │   ├── register/         # Registration page
│       │   ├── forgot-password/  # Password reset request
│       │   ├── reset-password/   # Password reset form
│       │   └── verify-email/     # Email verification
│       ├── components/           # Reusable UI components
│       ├── context/              # React contexts (Auth, Cart, Toast)
│       └── lib/                  # API utilities
│
├── verification-service/         # Tomcat WAR for email/password links
├── products.csv                  # Product seed data
└── Demo Pictures/                # Screenshots
```

---

## Services

### 1. Smarty Commerce — Java Backend

The core backend RESTful API built with **Spring Boot 3.5**. Deployed as a WAR on Apache Tomcat.

**Key Features:**
- **JWT Authentication** — Stateless authentication with HMAC-signed tokens. Tokens are returned in the `Authorization` header on login.
- **Role-Based Access Control (RBAC)** — Two roles: `ROLE_USER` and `ROLE_ADMIN`. Admin endpoints are protected with `@PreAuthorize`.
- **Email Verification** — Registration triggers an AWS SES email with a verification link. Users must verify before logging in.
- **Password Reset** — Token-based password reset flow via email.
- **Product Catalog** — Full CRUD with categories, subcategories, attributes (brand, color, size, etc.), and multiple image URLs.
- **Shopping Cart** — Per-user cart with add, update quantity, remove, and clear operations.
- **Order Management** — Place orders from cart, track order status (PENDING, SHIPPED, DELIVERED, CANCELLED).
- **Wishlist** — Add/remove products to a per-user wishlist.
- **Comments** — Users can leave comments/reviews on products.
- **Internal Data Feed** — Secure endpoint (`/internal/data-feed/*`) that the recommendation service uses to fetch interaction data for model training.

**Database Entities:**
`UserEntity`, `RoleEntity`, `AuthorityEntity`, `AddressEntity`, `ProductEntity`, `CategoryEntity`, `MainCategoryEntity`, `CartEntity`, `CartItemEntity`, `OrderEntity`, `OrderItemEntity`, `CommentEntity`, `PasswordResetTokenEntity`

---

### 2. Recommendation Service — Python

A standalone microservice built with **FastAPI** that computes personalized product recommendations using a hybrid algorithm.

**Key Features:**
- **Hybrid Engine** — Combines Content-Based Filtering and Item-to-Item Collaborative Filtering.
- **Automatic Rebuilds** — Model is rebuilt every 30 minutes (configurable) via APScheduler.
- **Manual Rebuild** — Admin endpoint to trigger immediate rebuild.
- **Cold-Start Handling** — New products with no interaction data fall back to 100% content-based recommendations.
- **Popularity Fallback** — New users with no interactions receive popularity-based recommendations.
- **JWT-Secured** — Shares the same HMAC secret with the Java backend for inter-service authentication.
- **Health Check** — `/health` endpoint reports engine readiness, product/user counts, and last rebuild time.

**Endpoints:**
| Method | Path | Description |
|---|---|---|
| `GET` | `/recommendations/{userId}` | Get personalized recommendations |
| `GET` | `/health` | Engine health & readiness |
| `POST` | `/admin/rebuild` | Trigger manual model rebuild |

---

### 3. Frontend — Next.js

A modern, responsive e-commerce UI built with **Next.js 16** (App Router) and **React 19**.

**Key Features:**
- **Glassmorphism Design** — Premium UI with glass panels, gradients, and micro-animations.
- **Skeleton Loading** — Shimmer-animated loading states for all pages (product grid, cart, orders, profile, product detail).
- **Toast Notifications** — Global notification system replacing all browser alerts.
- **Cart Badge** — Real-time cart item count in the navbar with pop-in animation.
- **Product Thumbnails** — Multi-image gallery with clickable thumbnails on product detail pages.
- **API Proxy** — Next.js rewrites proxy all API calls through the frontend server, eliminating CORS issues.
- **Admin Dashboard** — Full admin panel with progressive loading:
  - Dashboard with KPI cards (users, products, orders, revenue)
  - Order status breakdown
  - Product CRUD (add, edit, delete)
  - User management
  - Order management
  - Recommendation engine health & manual rebuild

**State Management:**
| Context | Purpose |
|---|---|
| `AuthContext` | User authentication, JWT token storage, login/logout |
| `CartContext` | Global cart item count, synced across all pages |
| `ToastContext` | App-wide toast notification system |

---

### 4. Verification Service

A lightweight WAR deployed on a separate Tomcat instance (port 8088). Contains static HTML pages that handle email verification and password reset links sent via AWS SES.

---

## Recommendation Algorithm

The recommendation engine implements a **hybrid approach** combining two methods:

### Collaborative Filtering (CF)
1. Build a **user-item interaction matrix** from three behavioral signals:
   - Orders (weight = 3)
   - Wishlist additions (weight = 2)
   - Cart additions (weight = 1)
2. Compute **item-item cosine similarity** from the transposed interaction matrix.

### Content-Based Filtering (CBF)
1. Build a **product feature matrix** using catalog metadata:
   - Main category (one-hot encoded)
   - Subcategory name (one-hot encoded)
   - Brand (one-hot encoded)
   - Price (normalized)
2. Compute **item-item cosine similarity** from the feature matrix.

### Hybrid Blending
```
For warm products (with CF signal):
    final_score = α × CF_score + (1 - α) × CBF_score

For cold-start products (no CF signal):
    final_score = CBF_score
```
- **α = 0.7** (70% CF, 30% CBF) by default, configurable via `ALPHA_CF` environment variable.

### Recommendation Generation
For a given user:
1. Identify all products the user has interacted with (ordered, wishlisted, carted).
2. For each interacted product, find the most similar products using the hybrid similarity matrix.
3. Aggregate scores across all interacted products, weighted by interaction strength.
4. Exclude already-ordered products from results.
5. Return the top-N scored products as recommendations.

---

## Database Schema

```
┌──────────┐     ┌──────────────┐     ┌────────────┐
│  users   │────►│  addresses   │     │   roles    │
│          │     └──────────────┘     └──────┬─────┘
│          │────►┌──────────────┐            │
│          │     │    carts     │     ┌──────▼──────┐
│          │     │   ┌─────────►│     │ authorities │
│          │     │   │cart_items│     └─────────────┘
│          │     └───┴─────────┘
│          │────►┌──────────────┐     ┌──────────────┐
│          │     │   orders     │     │  products    │
│          │     │   ┌─────────►│◄────│              │
│          │     │   │order_item│     │  ┌──────────►│
│          │     └───┴─────────┘     │  │ categories │
│          │────►┌──────────────┐     │  └───────────┘
│          │     │  wishlists   │     │  ┌───────────┐
│          │     └──────────────┘     │  │main_categ.│
│          │────►┌──────────────┐     │  └───────────┘
│          │     │  comments    │◄────┘
└──────────┘     └──────────────┘
```

---

## API Endpoints

### Authentication
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/users` | No | Register a new user |
| `POST` | `/users/login` | No | Login (returns JWT in header) |
| `GET` | `/users/email-verification` | No | Verify email with token |
| `POST` | `/users/password-reset-request` | No | Request password reset email |
| `POST` | `/users/password-reset` | No | Reset password with token |

### Users
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/users` | Admin | List all users (paginated) |
| `GET` | `/users/{userId}` | User | Get user details |
| `PUT` | `/users/{userId}` | User | Update user profile |
| `DELETE` | `/users/{userId}` | User | Delete user account |

### Products
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/products` | No | List products (search, filter, sort, paginate) |
| `GET` | `/products/{id}` | No | Get product details |
| `POST` | `/products` | Admin | Create product |
| `PUT` | `/products/{id}` | Admin | Update product |
| `DELETE` | `/products/{id}` | Admin | Delete product |

### Categories
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/categories` | No | List all categories |
| `POST` | `/categories` | Admin | Create category |
| `PUT` | `/categories/{id}` | Admin | Update category |
| `DELETE` | `/categories/{id}` | Admin | Delete category |

### Cart
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/users/{userId}/cart` | User | Get user's cart |
| `POST` | `/users/{userId}/cart/items` | User | Add item to cart |
| `PUT` | `/users/{userId}/cart/items/{itemId}` | User | Update item quantity |
| `DELETE` | `/users/{userId}/cart/items/{itemId}` | User | Remove item from cart |
| `DELETE` | `/users/{userId}/cart` | User | Clear entire cart |

### Orders
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/users/{userId}/orders` | User | Get user's orders |
| `POST` | `/users/{userId}/orders` | User | Place a new order |
| `GET` | `/orders` | Admin | List all orders |

### Wishlist & Comments
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET/POST/DELETE` | `/users/{userId}/wishlist` | User | Manage wishlist |
| `GET/POST/PUT/DELETE` | `/products/{id}/comments` | User | Manage comments |

---

## Screenshots

| Page | Preview |
|---|---|
| **Main Page** | ![Main Page](Demo%20Pictures/Main%20Page.png) |
| **Products** | ![Products](Demo%20Pictures/Products%20section.png) |
| **Recommendations** | ![Recommendations](Demo%20Pictures/Recommended%20products%20section.png) |
| **Product Details** | ![Product Details](Demo%20Pictures/Product%20details%20page.png) |
| **Shopping Cart** | ![Cart](Demo%20Pictures/Shopping%20cart%20page.png) |
| **Checkout** | ![Checkout](Demo%20Pictures/Checkout%20page.png) |
| **Wishlist** | ![Wishlist](Demo%20Pictures/Wishlist%20page.png) |
| **Profile** | ![Profile](Demo%20Pictures/My%20Profile%20Page%201.png) |
| **Login** | ![Login](Demo%20Pictures/Login%20Page.png) |
| **Register** | ![Register](Demo%20Pictures/Register%20Page.png) |
| **Admin Dashboard** | ![Admin](Demo%20Pictures/Admin%20panel%20dashoard.png) |

---

## Getting Started

### Prerequisites

- **Java 17** + **Maven**
- **Python 3.11+**
- **Node.js 18+** + **npm**
- **PostgreSQL** (or use H2 for development)
- **Apache Tomcat 10+** (for WAR deployment)
- **AWS Account** with SES configured (for email features)

### Local Development

#### 1. Start the Java Backend

```bash
cd smarty-commerce/smarty-commerce
mvn clean package -DskipTests
# Run with embedded server for development:
mvn spring-boot:run
# Or deploy the WAR to Tomcat
```

The backend will start on `http://localhost:8080/smarty-commerce`.

#### 2. Start the Recommendation Service

```bash
cd recommendation-service
python -m venv venv
source venv/bin/activate  # Windows: .\venv\Scripts\activate
pip install -r requirements.txt
# Create .env file (see Environment Variables section)
python main.py
```

The recommendation service will start on `http://localhost:8000`.

#### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:3000`.

### AWS EC2 Deployment

#### Backend (Tomcat)
```bash
# Deploy both WARs to the same Tomcat instance on port 8080:
# - smarty-commerce.war   → http://hostname:8080/smarty-commerce
# - verification-service.war → http://hostname:8080/verification-service
```
> **Note:** During local development with IDEs, the verification-service was run on a separate Tomcat instance (port 8088) to avoid conflicts. In production (EC2), both WARs can be deployed to the **same Tomcat** on port 8080 — each WAR is served under its own context path. If you deploy both to a single Tomcat, update the verification URLs in `AmazonSES.java` to point to port 8080 instead of 8088.

#### Recommendation Service (PM2)
```bash
cd recommendation-service
pm2 start "uvicorn main:app --host 0.0.0.0 --port 8000" --name "recommendation-service"
```

#### Frontend (Next.js)
```bash
cd frontend
npm run build
npm run start  # Starts on port 3000
# Or use PM2:
pm2 start "npm run start" --name "frontend"
```

> **Note:** The frontend uses Next.js rewrites to proxy API calls (`/api/core/*` → Java backend, `/api/rec/*` → recommendation service), eliminating CORS issues entirely.

---

## Environment Variables

### Recommendation Service (`.env`)
```env
SMARTY_COMMERCE_BASE_URL=http://localhost:8080/smarty-commerce
TOKEN_SECRET=your_jwt_secret_here
REBUILD_INTERVAL_MINUTES=30
DEFAULT_TOP_N=10
PORT=8000
ALPHA_CF=0.7
```

### Frontend (`.env.local`)
```env
NEXT_PUBLIC_CORE_API_URL=http://localhost:8080/smarty-commerce
NEXT_PUBLIC_REC_API_URL=http://localhost:8000
```

### Java Backend (`application.properties`)
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/smarty_commerce
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
token.secret=your_jwt_secret_here
```

> **Important:** The `TOKEN_SECRET` / `token.secret` must be the **same value** across the Java backend and the Python recommendation service for JWT inter-service authentication to work.

---

## License

This project was developed as a graduation project and is intended for educational purposes.
