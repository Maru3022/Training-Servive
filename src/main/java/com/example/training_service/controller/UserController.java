package com.example.training_service.controller;

import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.dto.UserDTO;
import com.example.training_service.dto.UserStatsDTO;
import com.example.training_service.exception.ErrorResponse;
import com.example.training_service.model.TrainingStatus;
import com.example.training_service.service.StatsService;
import com.example.training_service.service.TrainingService;
import com.example.training_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final TrainingService trainingService;
    private final StatsService statsService;

    public UserController(UserService userService,
                          TrainingService trainingService,
                          StatsService statsService) {
        this.userService = userService;
        this.trainingService = trainingService;
        this.statsService = statsService;
    }

    @Operation(summary = "Create a new user", description = "Creates a new user account with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @Operation(summary = "Get user by ID", description = "Returns a single user by their UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @Operation(summary = "Update user", description = "Fully replaces a user's details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict — duplicate username/email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @Operation(summary = "Soft-delete user", description = "Marks the user as inactive without removing data")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deactivated"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Hard-delete user", description = "Permanently removes a user and all associated data")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted permanently"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteUser(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        userService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get trainings for a user", description = "Returns a paginated list of all training sessions belonging to the specified user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/trainings")
    public ResponseEntity<Page<TrainingDTO>> getUserTrainings(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        // Verify user exists first
        userService.getById(id);
        return ResponseEntity.ok(trainingService.getTrainingsByUser(id, pageable));
    }

    @Operation(summary = "Get user training statistics", description = "Returns aggregated training statistics for the specified user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics computed successfully",
                    content = @Content(schema = @Schema(implementation = UserStatsDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(statsService.computeStats(id));
    }
}
