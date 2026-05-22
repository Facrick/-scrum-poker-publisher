package com.company.scrumpoker.user.service;

import com.company.scrumpoker.common.exception.BadRequestException;
import com.company.scrumpoker.jwt.JwtService;
import com.company.scrumpoker.user.dto.AuthRequest;
import com.company.scrumpoker.user.entity.UserEntity;
import com.company.scrumpoker.user.repository.UserRepository;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Бизнес-логика")
@Feature("Аутентификация")
@DisplayName("Тесты сервиса аутентификации (AuthService)")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void register_whenUsernameIsFree_shouldReturnToken() {
        // Arrange
        var request = new AuthRequest("newUser", "password123");
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(jwtService.generateToken(any())).thenReturn("test-token");

        // Act
        var response = authService.register(request);

        // Assert
        assertThat(response.token()).isEqualTo("test-token");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Ошибка регистрации: имя пользователя уже занято")
    void register_whenUsernameExists_shouldThrowException() {
        // Arrange
        var request = new AuthRequest("existingUser", "password123");
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username is already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешный вход в систему")
    void login_whenCredentialsAreValid_shouldReturnToken() {
        // Arrange
        var request = new AuthRequest("user", "password");
        var userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("hashedPassword")
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(userEntity));
        when(jwtService.generateToken(any())).thenReturn("test-token");

        // Act
        var response = authService.login(request);

        // Assert
        assertThat(response.token()).isEqualTo("test-token");
        // Проверяем, что AuthenticationManager был вызван для проверки пароля
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Ошибка входа: неверный пароль")
    void login_whenPasswordIsInvalid_shouldThrowException() {
        // Arrange
        var request = new AuthRequest("user", "wrong-password");
        // Мокируем AuthenticationManager, чтобы он выбросил исключение, как это делает Spring Security
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
