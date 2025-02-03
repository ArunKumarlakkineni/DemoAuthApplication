package com.arunAssessment.DemoApplication.service;

import com.arunAssessment.DemoApplication.entity.User;
import com.arunAssessment.DemoApplication.repo.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Value("${admin.registration.code}")
    private String adminRegistrationCode; // Inject the admin code from properties
    @Autowired
    private RestTemplate restTemplate;

    public User registerUser(User user, String adminCode) {
        // Admin registration validation
        if ("ADMIN".equals(user.getRole())) {
            if (!adminRegistrationCode.equals(adminCode)) {
                throw new RuntimeException("Invalid admin code. Registration denied.");
            }
        } else {
            user.setRole("USER"); // Default role for normal users
        }

        // Get IP Address
        String ipAddress = restTemplate.getForObject("https://api.ipify.org?format=json", String.class);
        ObjectMapper mapper = new ObjectMapper();
        String ip = "";
        try {
            JsonNode root = mapper.readTree(ipAddress);
            ip = root.get("ip").asText();
            user.setIpAddress(ip);

            // Get Country
            String countryInfo = restTemplate.getForObject("http://ip-api.com/json/" + ip, String.class);
            JsonNode countryRoot = mapper.readTree(countryInfo);
            user.setCountry(countryRoot.get("country").asText());
        } catch (Exception e) {
            // Handle exception
        }
        return userRepository.save(user);
    }

    public Optional<User> validateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && password.equals(userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUserByEmail(String email) {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }
}

