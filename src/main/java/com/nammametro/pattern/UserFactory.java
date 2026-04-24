package com.nammametro.pattern;

import com.nammametro.model.Admin;
import com.nammametro.model.Operator;
import com.nammametro.model.Passenger;
import com.nammametro.model.User;
import com.nammametro.model.enums.UserRole;

public class UserFactory {

    // Creational Pattern: Factory Pattern

    /**
     * Holds the result of user creation: a base User entity
     * and the corresponding role-specific entity.
     */
    public static class UserCreationResult {
        private final User user;
        private final Object roleEntity; // Passenger, Operator, or Admin

        public UserCreationResult(User user, Object roleEntity) {
            this.user = user;
            this.roleEntity = roleEntity;
        }

        public User getUser() {
            return user;
        }

        public Object getRoleEntity() {
            return roleEntity;
        }
    }

    /**
     * Factory method — creates a User and its role-specific entity
     * based on the provided role string.
     *
     * @param role           one of "PASSENGER", "OPERATOR", "ADMIN"
     * @param name           user's display name
     * @param email          user's email address
     * @param hashedPassword BCrypt-hashed password
     * @return UserCreationResult containing both the User and role entity
     * @throws IllegalArgumentException if the role is unrecognized
     */
    // Creational Pattern: Factory Pattern — this method decides which concrete
    // role entity to instantiate based on the role parameter.
    public static UserCreationResult createUser(String role, String name,
                                                 String email, String hashedPassword) {

        // 1. Create the base User with the appropriate role enum
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashedPassword);

        switch (role.toUpperCase()) {
            case "PASSENGER" -> {
                user.setRole(UserRole.PASSENGER);
                Passenger passenger = new Passenger();
                passenger.setUser(user);
                return new UserCreationResult(user, passenger);
            }
            case "OPERATOR" -> {
                user.setRole(UserRole.OPERATOR);
                Operator operator = new Operator();
                operator.setUser(user);
                operator.setEmployeeId("EMP-" + System.currentTimeMillis());
                return new UserCreationResult(user, operator);
            }
            case "ADMIN" -> {
                user.setRole(UserRole.ADMIN);
                Admin admin = new Admin();
                admin.setUser(user);
                admin.setAccessLevel("FULL");
                return new UserCreationResult(user, admin);
            }
            default -> throw new IllegalArgumentException(
                    "Unknown role: " + role + ". Expected PASSENGER, OPERATOR, or ADMIN.");
        }
    }
}
