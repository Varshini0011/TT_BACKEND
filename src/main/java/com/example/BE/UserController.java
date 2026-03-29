package com.example.BE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    // ─── REGISTER ──────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User with this email already exists!");
        }
        Users savedUser = userRepository.save(user);
        ActivityLog log = new ActivityLog(savedUser.getEmail(), "REGISTER");
        activityLogRepository.save(log);
        return ResponseEntity.ok(savedUser);
    }

    // ─── LOGIN ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public Users login(@RequestBody Users user) {
        Users existingUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            ActivityLog log = new ActivityLog(existingUser.getEmail(), "LOGIN");
            activityLogRepository.save(log);
            return existingUser;
        }
        return null;
    }

    // ─── GET All Users (Admin) ─────────────────────────────────────────────────
    @GetMapping("/all")
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // ─── GET All Workers ───────────────────────────────────────────────────────
    @GetMapping("/workers")
    public List<Users> getWorkers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null &&
                        (u.getRole().equalsIgnoreCase("MAINTENANCE_WORKER")
                        || u.getRole().equalsIgnoreCase("WORKER")
                        || u.getRole().equalsIgnoreCase("MAINTAINANCE WORKER")))
                .collect(Collectors.toList());
    }

    // ─── GET Activity Logs (Admin) ─────────────────────────────────────────────
    @GetMapping("/activity-logs")
    public List<ActivityLog> getActivityLogs() {
        return activityLogRepository.findAll();
    }
}