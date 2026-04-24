package com.nammametro.service;

import com.nammametro.model.Admin;
import com.nammametro.model.Operator;
import com.nammametro.model.Passenger;
import com.nammametro.model.User;
import com.nammametro.pattern.UserFactory;
import com.nammametro.repository.AdminRepository;
import com.nammametro.repository.OperatorRepository;
import com.nammametro.repository.PassengerRepository;
import com.nammametro.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration and login business logic.
 *
 * SRP: This class has one responsibility — orchestrating authentication
 *      workflows (register, login). It delegates password hashing to
 *      PasswordEncoder, token generation to JwtUtil, and entity creation
 *      to the UserFactory.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final OperatorRepository operatorRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PassengerRepository passengerRepository,
                       OperatorRepository operatorRepository,
                       AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
        this.operatorRepository = operatorRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user using the Factory Pattern.
     *
     * @return the saved User entity
     * @throws IllegalArgumentException if email already exists or role is invalid
     */
    @Transactional
    public User register(String name, String email, String password, String role) {
        // Check for duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Hash the password using BCrypt
        String hashedPassword = passwordEncoder.encode(password);

        // Use Factory Pattern to create User + role entity
        // Creational Pattern: Factory Pattern
        UserFactory.UserCreationResult result =
                UserFactory.createUser(role, name, email, hashedPassword);

        // Persist the base user first (needed for FK references)
        User savedUser = userRepository.save(result.getUser());

        // Persist the role-specific entity
        Object roleEntity = result.getRoleEntity();
        if (roleEntity instanceof Passenger passenger) {
            passenger.setUser(savedUser);
            passengerRepository.save(passenger);
        } else if (roleEntity instanceof Operator operator) {
            operator.setUser(savedUser);
            operatorRepository.save(operator);
        } else if (roleEntity instanceof Admin admin) {
            admin.setUser(savedUser);
            adminRepository.save(admin);
        }

        return savedUser;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @return JWT token string
     * @throws AuthenticationException if credentials are invalid
     */
    public String login(String email, String password) {
        // Delegate credential validation to Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // If authentication succeeds, generate JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}
