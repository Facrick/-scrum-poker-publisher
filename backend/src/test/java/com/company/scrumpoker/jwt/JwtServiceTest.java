package com.company.scrumpoker.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Epic("Инфраструктура")
@Feature("JWT")
@DisplayName("Тесты сервиса JWT (JwtService)")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private UserDetails otherUserDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "c3BydW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLWV4YW1wbGUtc2VjcmV0LWtleQo=");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 час

        userDetails = new User("testuser", "password", new ArrayList<>());
        otherUserDetails = new User("otheruser", "password", new ArrayList<>());
    }

    @Test
    @DisplayName("Имя пользователя корректно извлекается из токена")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Сгенерированный токен валиден для правильного пользователя")
    void isTokenValid_whenTokenIsValid_shouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Токен невалиден для другого пользователя")
    void isTokenValid_whenUsernameDoesNotMatch_shouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, otherUserDetails);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Просроченный токен не проходит валидацию")
    void isTokenValid_whenTokenIsExpired_shouldThrowException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(userDetails);
        Thread.sleep(2);

        // Ожидаем, что метод выбросит ExpiredJwtException
        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
