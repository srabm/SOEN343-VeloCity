package com.concordia.velocity.controller;

import com.concordia.velocity.model.User;
import com.concordia.velocity.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/me
     * Retrieve the current user's information.
     * (For now, this uses a hardcoded userId until authentication is added.)
     */
    @GetMapping("/me")
    public Object getUserInfo() throws ExecutionException, InterruptedException {
        String userId = "U001"; // TODO: Replace with authenticated user ID later
        User user = userService.getUserById(userId);

        if (user == null) {
            return Map.of("error", "User not found: " + userId);
        }

        return user;
    }

    /**
     * POST /api/users
     * Create a new user (email must be unique)
     */
    @PostMapping
    public Object createUser(@RequestBody User user) throws ExecutionException, InterruptedException {
        return userService.createUser(user);
    }

    /**
     * PUT /api/users/me
     * Update the current user's information
     */
    @PutMapping("/me")
    public Object updateUserInfo(@RequestBody Map<String, Object> updates)
            throws ExecutionException, InterruptedException {
        String userId = "U001"; // TODO: Replace with authenticated user ID later
        return userService.updateUser(userId, updates);
    }

    /**
     * DELETE /api/users/me
     * Delete the current user's account
     */
    @DeleteMapping("/me")
    public Object deleteUser() throws ExecutionException, InterruptedException {
        String userId = "U001"; // TODO: Replace with authenticated user ID later
        return userService.deleteUser(userId);
    }
}
