# Namma Metro Project Explanation (Detailed)

This document explains the full codebase with a focus on OOAD concepts, design patterns, MVC architecture, SOLID, and GRASP. It is based on the current implementation in the repository.

## 1) What the system is

Namma Metro is a Spring Boot 3.2.x web application that manages metro operations and passenger services. It supports three roles:
- Passenger: search routes, estimate fares, book tickets, manage tickets, and receive notifications.
- Operator: manage schedules, update train status, report/resolve incidents.
- Admin: manage stations, routes, trains, view logs, and generate reports.

The project uses a layered MVC architecture with a clear separation between:
- Presentation (Thymeleaf templates and controllers)
- Business logic (services)
- Data access (repositories)
- Domain model (entities and enums)

## 2) MVC and Layered Architecture

### 2.1 Controller Layer (C in MVC)
Controllers receive HTTP requests, validate input, and delegate work to services. They also prepare data for views (Thymeleaf templates). Examples:
- Authentication flow: AuthController
- Passenger flows: PassengerSearchController, PassengerTicketController, PassengerNotificationController
- Operator flows: OperatorScheduleController, OperatorTrainController, OperatorIncidentController
- Admin flows: AdminStationController, AdminRouteController, AdminTrainController, AdminReportController, AdminLogController

Controllers are thin; they avoid direct data access and only use services. This matches the MVC best practice of keeping controllers as coordinators rather than domain logic holders.

### 2.2 Service Layer (Business Logic)
Services encapsulate the domain rules, validations, and workflows. Examples:
- AuthService: registration and login orchestration
- TicketService: ticket lifecycle rules and refunds
- ScheduledTaskService: time-driven ticket state changes
- TrainCancellationService: cascading ticket cancellation on train cancellation
- FareCalculator: fare rules orchestration (OCP)

This layer enforces business rules and centralizes logic shared by multiple controllers.

### 2.3 Repository Layer (Data Access)
Repositories extend Spring Data JPA and provide database operations using method names and JPQL where needed. Examples:
- RouteRepository uses JPQL queries to find routes by station pairs
- LogRepository exposes pagination and filter methods

Repositories are used only by services, not directly by controllers.

### 2.4 View Layer (V in MVC)
Thymeleaf templates under src/main/resources/templates render HTML pages for different roles and features. Static assets (CSS/JS) live in src/main/resources/static and are referenced by templates.

## 3) Domain Model (Entities, Enums, and Relationships)

### 3.1 Core entities
- User: base authentication and identity record
- Passenger, Operator, Admin: role-specific profiles linked to User
- Station: metro station entity
- Route: connects start and end stations and contains intermediate stops
- Train: assigned to a route with status and capacity
- Schedule: timing information for trains at stations
- Ticket: booking record with status, QR code, and refund amount
- Incident: operator-reported issues tied to trains
- Notification: user-facing alerts
- Report: admin-generated reports and metadata
- Log: audit trail entries

### 3.2 Key relationships
- User to Passenger/Operator/Admin is one-to-one
- Route to Station is many-to-one for start/end, and many-to-many for intermediates
- Train to Route is many-to-one
- Schedule to Train and Station are many-to-one
- Ticket to Passenger, Schedule, Source Station, Destination Station are many-to-one
- Notification and Log reference User

### 3.3 Enums as constrained state
Enums represent valid states and enforce domain constraints:
- UserRole: PASSENGER, OPERATOR, ADMIN
- TrainStatus: SCHEDULED, RUNNING, DELAYED, CANCELLED, COMPLETED, MAINTENANCE
- TicketStatus: BOOKED, CONFIRMED, CANCELLED, USED, EXPIRED
- ScheduleStatus: ACTIVE, CANCELLED, COMPLETED
- IncidentStatus: OPEN, RESOLVED
- Severity: LOW, MEDIUM, HIGH
- NotificationType: DELAY, CANCELLATION, BOOKING_CONFIRMED, BOOKING_CANCELLED, TICKET_EXPIRED, GENERAL

## 4) Design Patterns (with code mapping)

### 4.1 Singleton: DatabaseManager
- Purpose: ensure a single database configuration instance.
- Implementation: lazy instantiation with double-checked locking and a private constructor.
- OOAD idea: controlled access to shared resource configuration.

### 4.2 Factory: UserFactory
- Purpose: create a base User and its role-specific entity in one place.
- Encapsulates how Passenger/Operator/Admin objects are created and linked.
- Used by AuthService during registration, keeping controllers and services from knowing concrete creation logic.

### 4.3 State: TrainState and TrainStateContext
- Purpose: enforce valid train status transitions.
- TrainStateContext maps a TrainStatus to a concrete state and validates transitions.
- Used in OperatorTrainController so invalid transitions are blocked consistently.

### 4.4 Observer: TrainStatusPublisher and observers
- Purpose: react to train status changes without tightly coupling to the update logic.
- TrainStatusPublisher is the subject; observers implement TrainStatusObserver.
- PassengerNotificationObserver: creates notifications for affected passengers on DELAYED or CANCELLED.
- IncidentLogObserver: records automatic logs for status changes.

### 4.5 Adapter: NotificationSender and adapters
- Purpose: standardize multiple notification channels behind one interface.
- EmailNotificationAdapter, SMSNotificationAdapter, AppNotificationAdapter provide channel-specific behavior.
- This is also a DIP use-case: higher-level services can depend on NotificationSender rather than concrete channels.

## 5) SOLID Principles (Mapping to code)

### 5.1 Single Responsibility Principle (SRP)
Nearly every class is focused on one responsibility:
- Controllers: request routing and model population only.
- Services: business rules and workflows.
- Repositories: persistence access.
- Entities: state only.
- Example: TicketService contains refund logic and ticket state transitions only.

### 5.2 Open/Closed Principle (OCP)
- BaseEntity provides shared fields so new entities can extend it without modifying existing code.
- FareRule and FareCalculator allow adding new pricing rules without changing calculation logic.

### 5.3 Liskov Substitution Principle (LSP)
- Implementations of FareRule can be substituted without changing FareCalculator behavior.
- TrainState implementations are interchangeable through TrainStateContext.

### 5.4 Interface Segregation Principle (ISP)
- IAdminService, IOperatorService, IPassengerService, IStationService, IRouteService, ITrainService keep contracts small and role-focused.
- Admin code never depends on passenger or operator contracts.

### 5.5 Dependency Inversion Principle (DIP)
- NotificationSender abstracts delivery channels.
- Higher-level services depend on abstractions, not concrete email/SMS/app implementations.

## 6) GRASP Principles (Mapping to code)

### 6.1 Information Expert
- TicketService owns ticket state transitions and refund logic since it knows ticket structure and status.
- FareCalculator owns fare computation rules since it aggregates pricing logic.
- AdminService owns report calculations because it coordinates data across tickets, routes, and logs.

### 6.2 Creator
- UserFactory is the creator for Passenger/Operator/Admin objects because it knows the creation rules and linkage to User.
- PassengerTicketController creates Ticket objects and delegates persistence to TicketService.

### 6.3 Controller
- MVC controllers act as GRASP Controllers by receiving user requests and coordinating the domain operations.
- Example: PassengerTicketController orchestrates booking but does not implement refund rules itself.

### 6.4 Low Coupling / High Cohesion
- Services rely on repositories rather than raw entity managers.
- Controllers depend on services rather than repositories or entities directly.
- Each package has a clear purpose: controller, service, repository, model, pattern, config.

### 6.5 Polymorphism
- FareRule implementations provide different behaviors for the same interface.
- TrainState implementations provide state-specific transitions.
- NotificationSender adapters provide different channels with the same interface.

### 6.6 Protected Variations
- FareRule and NotificationSender shield higher-level logic from changes in pricing or notification channels.
- TrainStateContext shields status transition logic from controller changes.

### 6.7 Pure Fabrication
- AuditLogService provides logging logic that does not naturally belong in any single entity.
- ScheduledTaskService encapsulates time-based behavior, separate from entities.

## 7) Security Architecture

### 7.1 JWT and Spring Security
- SecurityConfig defines stateless security and role-based access rules.
- JwtAuthenticationFilter reads JWT from an HTTP-only cookie and populates the security context.
- CustomUserDetailsService bridges User to Spring Security UserDetails.

### 7.2 Role-based access control
- URL patterns are locked by role:
  - /passenger/** -> PASSENGER
  - /operator/** -> OPERATOR
  - /admin/** -> ADMIN
- Public routes like /, /auth/**, and /schedules/public/** remain open.

## 8) Key Business Workflows (End-to-End)

### 8.1 Registration and Login
1) AuthController validates form input.
2) AuthService checks for duplicates and hashes the password.
3) UserFactory creates base User + role entity.
4) AuthService saves the entities and issues JWT on login.
5) JWT stored in HTTP-only cookie; SecurityConfig validates requests.

### 8.2 Passenger Route Search
1) PassengerSearchController validates source/destination.
2) Routes are filtered by start/end station matching.
3) FareCalculator computes fare estimates per route.
4) Schedules are collected for matching routes.

### 8.3 Ticket Booking and Confirmation
1) PassengerTicketController resolves Passenger from the security context.
2) Ticket is created with BOOKED status and QR code.
3) FareCalculator generates final fare.
4) NotificationService creates booking notification.
5) Ticket can be confirmed, cancelled, or expired based on TicketService rules.

### 8.4 Scheduled Ticket Lifecycle
1) ScheduledTaskService runs every 5 minutes.
2) Tickets with past departures are EXPIRED.
3) Tickets older than 10 minutes in BOOKED state are auto-cancelled.
4) Notifications are created for both changes.

### 8.5 Operator Train Status Change
1) OperatorTrainController loads Train and requested status.
2) TrainStateContext validates the transition.
3) TrainStatusPublisher notifies observers.
4) PassengerNotificationObserver notifies passengers.
5) IncidentLogObserver adds log entries.
6) TrainCancellationService auto-cancels tickets if train is CANCELLED.

### 8.6 Admin Reports and Logs
1) AdminReportController calls AdminService to aggregate revenue or usage data.
2) Reports are saved to the reports table.
3) AdminLogController renders logs with filtering and pagination.

## 9) Database and Persistence

### 9.1 Schema and initialization
- schema.sql defines 13 tables and relationships.
- application.properties configures MySQL, JPA, and SQL initialization.

### 9.2 JPA mapping strategy
- Entities are JPA-annotated with relationships matching the schema.
- BaseEntity provides consistent id and timestamps for many domain objects.

## 10) UI/UX and Static Assets

The UI is server-rendered with Thymeleaf templates.
- Templates are organized by role: admin, operator, passenger, public.
- static/css/style.css defines the visual system.
- static/css/theme.css and static/js/theme.js implement a dark/light theme toggle.

## 11) Notes on Quality Attributes

### 11.1 Maintainability
- Patterns isolate change: fare rules, notification channels, and train status transitions are extensible.
- Interfaces keep service responsibilities narrow.

### 11.2 Scalability
- Stateless JWT auth enables horizontal scaling.
- Scheduled tasks and observers decouple cross-cutting effects.

### 11.3 Testability
- Services are small and can be unit tested in isolation.
- Repositories can be tested with Spring Data test slices.

## 12) Suggested walkthrough order (for reviewers)

1) Application entry and security: NammaMetroApplication, SecurityConfig, JwtAuthenticationFilter.
2) Domain model: User, Passenger/Operator/Admin, Station, Route, Train, Schedule, Ticket.
3) Services: AuthService, TicketService, ScheduledTaskService, TrainCancellationService.
4) Patterns: UserFactory, TrainStateContext, TrainStatusPublisher, NotificationSender adapters.
5) Controllers: Passenger, Operator, Admin flows.
6) Templates and static assets for UI behavior.

---

If you want, I can expand this into a per-class explanation section (class by class) or generate sequence diagrams for the major flows.