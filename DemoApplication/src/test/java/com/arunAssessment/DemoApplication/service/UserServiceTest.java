package com.arunAssessment.DemoApplication.service;

import com.arunAssessment.DemoApplication.entity.User;
import com.arunAssessment.DemoApplication.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "adminRegistrationCode", "ADMIN");
    }

    @Test
    void testRegisterUser_WithAdminCode() {
        User user = new User();
        user.setRole("ADMIN");

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{\"ip\":\"192.168.0.1\"}", "{\"country\":\"India\"}");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registerUser(user, "ADMIN");

        assertNotNull(registeredUser);
        assertEquals("192.168.0.1", registeredUser.getIpAddress());
        assertEquals("India", registeredUser.getCountry());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testValidateUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.validateUser("test@example.com", "password123");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testValidateUser_Failure() {
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.validateUser(null, "hjhdj");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void testDeleteUserByEmail() {
        User user = new User();
        user.setEmail("delete@example.com");

        when(userRepository.findByEmail("delete@example.com")).thenReturn(Optional.of(user));

        userService.deleteUserByEmail("delete@example.com");

        verify(userRepository, times(1)).delete(user);
    }
}