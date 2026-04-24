# 🚇 Namma Metro — Operations & Information Management System

A comprehensive metro management system built with **Spring Boot 3.2.5**, implementing multiple **Design Patterns** and **SOLID Principles** with role-based access control for Passengers, Operators, and Admins.

---

## 📋 Features

### 🔐 Authentication & Security
- Spring Security with **JWT** (JSON Web Token) authentication
- Role-based access: **Passenger**, **Operator**, **Admin**
- BCrypt password hashing
- Role-specific dashboards with protected routes

### 🏛️ Admin Module
- **CRUD** for Stations, Routes, and Trains
- **Revenue Report** with Chart.js bar charts (filterable by date & route)
- **Usage Report** with doughnut, bar, and line charts (tickets/route, busiest stations, peak hours)
- **System Logs** viewer — paginated, filterable by action type
- Dashboard with live stats (stations, routes, trains, passengers)

### 🚆 Operator Module
- **Schedule Management** — create/modify train schedules
- **Train Status Updates** — RUNNING, DELAYED, CANCELLED, MAINTENANCE (validated via State Pattern)
- **Incident Management** — report and resolve incidents with severity levels
- Audit logging of every operator action

### 🎫 Passenger Module
- **Route Search** — find routes between any two stations
- **Fare Calculation** — base fare + per-km charges + metro card discount (10%)
- **Ticket Booking** — with unique UUID QR code
- **Ticket Lifecycle** — BOOKED → CONFIRMED → USED / CANCELLED / EXPIRED
- **Refund System** — 100% (>1hr before departure), 50% (otherwise)
- **QR Code** — ZXing-generated 250×250 PNG on ticket detail page
- **Booking History** — paginated with status badges
- **Notification Center** — categorized, color-coded, mark as read

### ⚙️ Automated System
- `@Scheduled` ticket expiration (every 5 minutes)
- Auto-cancel unpaid bookings (>10 min timeout)
- Cascading ticket cancellation when train is cancelled (with 100% refund)

### 🎨 UI/UX
- **Dark Mode** by default with **Light Mode toggle** (☀️/🌙)
- Metro-inspired dark theme with glassmorphism effects
- Theme preference saved to `localStorage`
- Custom error pages (404, 403, 500)
- Responsive design

---

## 🏗️ Design Patterns

| # | Pattern | Type | Implementation | Comment Tag |
|---|---------|------|----------------|-------------|
| 1 | **Singleton** | Creational | `DatabaseManager.java` — centralized DB configuration | `// Singleton Design Pattern` |
| 2 | **Factory** | Creational | `UserFactory.java` — creates User subclasses based on role | `// Creational Pattern: Factory Pattern` |
| 3 | **State** | Behavioral | `TrainState` interface + `RunningState`, `DelayedState`, `CancelledState`, `MaintenanceState`, `ScheduledState` | `// Behavioral Pattern: State Pattern` |
| 4 | **Observer** | Behavioral | `TrainStatusObserver` + `TrainStatusPublisher` + `PassengerNotificationObserver` + `IncidentLogObserver` | `// Behavioral Pattern: Observer Pattern` |
| 5 | **Adapter** | Structural | `NotificationSender` + `EmailNotificationAdapter`, `SMSNotificationAdapter`, `AppNotificationAdapter` | `// Structural Pattern: Adapter Pattern` |

---

## 📐 SOLID Principles

| # | Principle | Application |
|---|-----------|-------------|
| 1 | **Single Responsibility (SRP)** | Every class has one responsibility — documented in every file |
| 2 | **Open/Closed (OCP)** | `BaseEntity` for extensible entities; `FareRule` interface for pluggable fare rules |
| 3 | **Interface Segregation (ISP)** | `IPassengerService`, `IOperatorService`, `IAdminService` — no class depends on methods it doesn't need |
| 4 | **Dependency Inversion (DIP)** | `NotificationSender` abstraction — high-level modules depend on interface, not implementations |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 3.2.5 |
| Frontend | Thymeleaf + CSS3 + Chart.js |
| Database | MySQL 8.0 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| ORM | Spring Data JPA / Hibernate |
| QR Code | ZXing 3.5.3 |
| Build | Maven |

---

## 🚀 Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Clone the repository
```bash
git clone https://github.com/your-username/NammaMetro.git
cd NammaMetro
```

### 2. Create the MySQL database
```sql
CREATE DATABASE namma_metro;
```

### 3. Configure database credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/namma_metro
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

Or use environment variables:
```bash
set DB_USERNAME=root
set DB_PASSWORD=yourpassword
```

### 4. Build and run
```bash
mvn clean compile
mvn spring-boot:run
```

### 5. Access the application
Open `http://localhost:8080` in your browser.

### 6. Register users
- Go to `/auth/register` and create accounts with roles: `PASSENGER`, `OPERATOR`, or `ADMIN`
- Login at `/auth/login`

---

## 📁 Project Structure

```
src/main/java/com/nammametro/
├── config/          # Security, JWT filter
├── controller/      # 27 MVC controllers
├── model/           # JPA entities + enums
├── pattern/         # Design pattern implementations
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic + ISP interfaces

src/main/resources/
├── static/
│   ├── css/         # style.css + theme.css
│   └── js/          # theme.js
├── templates/
│   ├── admin/       # Admin CRUD, reports, logs
│   ├── auth/        # Login, register
│   ├── dashboard/   # Role-specific dashboards
│   ├── error/       # 404, 403, 500
│   ├── fragments/   # Shared navbar/footer
│   ├── operator/    # Schedules, trains, incidents
│   ├── passenger/   # Search, tickets, notifications
│   └── public/      # Public schedule view
├── application.properties
└── schema.sql       # 13-table MySQL schema
```

---

## 📊 Database Schema (13 Tables)

| Table | Purpose |
|-------|---------|
| `users` | All users with role (PASSENGER/OPERATOR/ADMIN) |
| `passengers` | Passenger profiles + metro card flag |
| `operators` | Operator profiles + employee ID |
| `admin` | Admin profiles + department |
| `stations` | Metro stations with code, name, line |
| `routes` | Routes with source, destination, distance, duration |
| `trains` | Train fleet with route assignment and status |
| `schedules` | Train schedules with times and dates |
| `tickets` | Booked tickets with fare, QR code, refund |
| `incidents` | Reported incidents with severity |
| `notifications` | User notifications with type categorization |
| `reports` | Generated reports with JSON parameters |
| `logs` | System audit trail |

---

## 📸 Screenshots

> Screenshots will be added here after deployment.

| Screen | Path |
|--------|------|
| Landing Page | `/` |
| Login | `/auth/login` |
| Passenger Dashboard | `/passenger/dashboard` |
| Route Search Results | `/passenger/search` |
| Ticket Detail + QR | `/passenger/tickets/{id}` |
| Admin Dashboard | `/admin/dashboard` |
| Revenue Report | `/admin/reports/revenue` |
| Usage Report | `/admin/reports/usage` |
| System Logs | `/admin/logs` |
| Operator Dashboard | `/operator/dashboard` |
| Public Schedules | `/schedules/public` |

---

## 📄 License

This project was built as an educational demonstration of **Design Patterns** and **SOLID Principles** in a Java Spring Boot application.

---

*Built with ❤️ for Namma Bengaluru 🚇*
