package com.concordia.velocity.controller;

import com.concordia.velocity.model.Rider;
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
        String userId = "U001"; // TODO: Replace with authenticated rider ID later
        Rider rider = userService.getUserById(userId);

        if (rider == null) {
            return Map.of("error", "Rider not found: " + userId);
        }

        return rider;
    }

    /**
     * POST /api/users
     * Create a new rider (email must be unique)
     */
    @PostMapping
    public Object createUser(@RequestBody Rider rider) throws ExecutionException, InterruptedException {
        return userService.createUser(rider);
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
