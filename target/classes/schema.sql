-- ============================================================
-- Namma Metro — Database Schema
-- Creates all 13 tables with proper relationships and FKs.
-- Every table has: id (PK, AUTO_INCREMENT), created_at, updated_at
-- ============================================================

-- 1. Users (base table for authentication & role management)
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL COMMENT 'Stored as a BCrypt hash',
    role        ENUM('PASSENGER', 'OPERATOR', 'ADMIN') NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Passengers (extends users with passenger-specific info)
CREATE TABLE IF NOT EXISTS passengers (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL UNIQUE,
    phone           VARCHAR(15),
    address         VARCHAR(255),
    has_metro_card  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Operators (extends users with operator-specific info)
CREATE TABLE IF NOT EXISTS operators (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL UNIQUE,
    employee_id  VARCHAR(50)  NOT NULL UNIQUE,
    department   VARCHAR(100),
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Admin (extends users with admin-specific info)
CREATE TABLE IF NOT EXISTS admin (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL UNIQUE,
    access_level  VARCHAR(50)  NOT NULL DEFAULT 'FULL',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Stations
CREATE TABLE IF NOT EXISTS stations (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(10)  NOT NULL UNIQUE,
    line        VARCHAR(50)  NOT NULL COMMENT 'e.g. Purple Line, Green Line',
    location    VARCHAR(255),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Routes
CREATE TABLE IF NOT EXISTS routes (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(100) NOT NULL,
    start_station_id  BIGINT       NOT NULL,
    end_station_id    BIGINT       NOT NULL,
    distance_km       DECIMAL(6,2),
    duration_min      INT,
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (start_station_id) REFERENCES stations(id),
    FOREIGN KEY (end_station_id)   REFERENCES stations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6b. Route Intermediate Stations (join table for ordered intermediate stops)
CREATE TABLE IF NOT EXISTS route_intermediate_stations (
    route_id       BIGINT NOT NULL,
    station_id     BIGINT NOT NULL,
    station_order  INT    NOT NULL DEFAULT 0,
    FOREIGN KEY (route_id)   REFERENCES routes(id) ON DELETE CASCADE,
    FOREIGN KEY (station_id) REFERENCES stations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Trains
CREATE TABLE IF NOT EXISTS trains (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    train_number  VARCHAR(20)  NOT NULL UNIQUE,
    name          VARCHAR(100),
    route_id      BIGINT,
    status        ENUM('SCHEDULED', 'RUNNING', 'DELAYED', 'CANCELLED', 'COMPLETED', 'MAINTENANCE') NOT NULL DEFAULT 'SCHEDULED',
    capacity      INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (route_id) REFERENCES routes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Schedules
CREATE TABLE IF NOT EXISTS schedules (
    id               BIGINT    NOT NULL AUTO_INCREMENT,
    train_id         BIGINT    NOT NULL,
    station_id       BIGINT    NOT NULL,
    arrival_time     TIME,
    departure_time   TIME,
    day_of_week      VARCHAR(10) COMMENT 'e.g. MONDAY, TUESDAY',
    schedule_date    DATE      COMMENT 'Specific date of this schedule',
    schedule_status  ENUM('ACTIVE', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (train_id)   REFERENCES trains(id),
    FOREIGN KEY (station_id) REFERENCES stations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Tickets
CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    passenger_id        BIGINT         NOT NULL,
    schedule_id         BIGINT         NOT NULL,
    source_station_id   BIGINT         NOT NULL,
    dest_station_id     BIGINT         NOT NULL,
    fare                DECIMAL(8,2)   NOT NULL,
    status              ENUM('BOOKED', 'CONFIRMED', 'CANCELLED', 'USED', 'EXPIRED') NOT NULL DEFAULT 'BOOKED',
    booking_time        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    qr_code             VARCHAR(100)   UNIQUE COMMENT 'UUID-based QR code string',
    refund_amount       DECIMAL(8,2)   COMMENT 'Amount refunded on cancellation',
    created_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (passenger_id)      REFERENCES passengers(id),
    FOREIGN KEY (schedule_id)       REFERENCES schedules(id),
    FOREIGN KEY (source_station_id) REFERENCES stations(id),
    FOREIGN KEY (dest_station_id)   REFERENCES stations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Incidents
CREATE TABLE IF NOT EXISTS incidents (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(150) NOT NULL,
    train_id         BIGINT,
    reported_by      BIGINT       NOT NULL,
    type             VARCHAR(50)  NOT NULL,
    description      TEXT,
    severity         ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'LOW',
    incident_status  ENUM('OPEN', 'RESOLVED') NOT NULL DEFAULT 'OPEN',
    resolved         BOOLEAN      NOT NULL DEFAULT FALSE,
    resolution_note  TEXT,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (train_id)    REFERENCES trains(id),
    FOREIGN KEY (reported_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    title             VARCHAR(150) NOT NULL,
    message           TEXT,
    notification_type ENUM('DELAY', 'CANCELLATION', 'BOOKING_CONFIRMED', 'BOOKING_CANCELLED', 'TICKET_EXPIRED', 'GENERAL') DEFAULT 'GENERAL',
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Reports
CREATE TABLE IF NOT EXISTS reports (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    generated_by  BIGINT       NOT NULL,
    title         VARCHAR(150) NOT NULL,
    type          VARCHAR(50)  NOT NULL,
    parameters    TEXT         COMMENT 'JSON string with report params (date range, route, etc.)',
    content       TEXT,
    generated_at  TIMESTAMP    NULL COMMENT 'When the report was generated',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (generated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Logs (audit trail)
CREATE TABLE IF NOT EXISTS logs (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT,
    action     VARCHAR(100) NOT NULL,
    entity     VARCHAR(50),
    entity_id  BIGINT,
    details    TEXT,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
