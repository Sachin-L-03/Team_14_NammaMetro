package com.nammametro.pattern;

public class DatabaseManager {

    // ---- Singleton instance (volatile for thread-safe lazy init) ----
    private static volatile DatabaseManager instance;

    // ---- Database configuration properties ----
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    /**
     * Private constructor — prevents instantiation from outside.
     * Initializes the database connection properties.
     */
    private DatabaseManager() {
        this.url             = "jdbc:mysql://localhost:3306/namma_metro?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
        this.username        = "root";
        this.password        = "root";
        this.driverClassName = "com.mysql.cj.jdbc.Driver";
    }

    /**
     * Returns the single instance of DatabaseManager.
     *
     * Uses double-checked locking:
     *   1st check  — avoids synchronization overhead when instance exists.
     *   2nd check  — inside synchronized block to prevent two threads from
     *                creating separate instances simultaneously.
     *
     * @return the singleton DatabaseManager instance
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {                        // 1st check (no lock)
            synchronized (DatabaseManager.class) {     // acquire lock
                if (instance == null) {                // 2nd check (with lock)
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // ---- Getters (read-only access to configuration) ----

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }
}
