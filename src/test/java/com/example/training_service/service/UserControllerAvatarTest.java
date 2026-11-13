package com.example.training_service.service;

import com.example.training_service.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerAvatarTest {

    private UserService userService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        controller = new UserController(userService, mock(TrainingService.class), mock(StatsService.class));
    }

    @Test
    void uploadAvatarRejectsSvgWithScript() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.svg", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadAvatar("7ef20a16-8e30-4ec9-b67c-bf201d174fa2", file));
        verifyNoInteractions(userService);
    }

    @Test
    void uploadAvatarRejectsHtmlDisguisedAsPng() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "<script>alert(1)</script>".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadAvatar("7ef20a16-8e30-4ec9-b67c-bf201d174fa2", file));
        verifyNoInteractions(userService);
    }
}
