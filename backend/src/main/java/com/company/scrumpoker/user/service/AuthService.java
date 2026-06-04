package com.company.scrumpoker.user.service;

import com.company.scrumpoker.common.exception.BadRequestException;
import com.company.scrumpoker.common.exception.NotFoundException;
import com.company.scrumpoker.jwt.JwtService;
import com.company.scrumpoker.user.dto.AuthRequest;
import com.company.scrumpoker.user.dto.AuthResponse;
import com.company.scrumpoker.user.entity.UserEntity;
import com.company.scrumpoker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken");
        }

        var user = UserEntity.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        var userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        var jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.username()));

        var userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        var jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken);
    }
}
