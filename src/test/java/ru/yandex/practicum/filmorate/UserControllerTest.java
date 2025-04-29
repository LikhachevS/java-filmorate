package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    private UserController controller;

    @BeforeEach
    public void setup() {
        controller = new UserController();
    }

    // Тест 1: Валидация не прошла (пустая почта)
    @Test
    public void testCreateUserWithEmptyEmailFailsValidation() {
        User user = new User();
        user.setEmail("");
        ValidationException thrown = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertThat(thrown.getMessage())
                .isEqualTo("Электронная почта не может быть пустой и должна содержать символ @.");
    }

    // Тест 2: Валидация не прошла (неправильная почта)
    @Test
    public void testCreateUserWithInvalidEmailFailsValidation() {
        User user = new User();
        user.setEmail("example.com");
        ValidationException thrown = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertThat(thrown.getMessage()).isEqualTo("Электронная почта не может быть пустой и должна содержать символ @.");
    }

    // Тест 3: Валидация не прошла (пустой логин)
    @Test
    public void testCreateUserWithEmptyLoginFailsValidation() {
        User user = new User();
        user.setEmail("example@example.com");
        user.setLogin("");
        ValidationException thrown = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertThat(thrown.getMessage()).isEqualTo("Логин не может быть пустым и содержать пробелы.");
    }

    // Тест 4: Валидация не прошла (логин с пробелами)
    @Test
    public void testCreateUserWithSpacedLoginFailsValidation() {
        User user = new User();
        user.setEmail("example@example.com");
        user.setLogin("inva lid");
        ValidationException thrown = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertThat(thrown.getMessage()).isEqualTo("Логин не может быть пустым и содержать пробелы.");
    }

    // Тест 5: Валидация не прошла (дата рождения в будущем)
    @Test
    public void testCreateUserWithFutureBirthdateFailsValidation() {
        User user = new User();
        user.setEmail("example@example.com");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.now().plusMonths(1));
        ValidationException thrown = assertThrows(ValidationException.class, () -> UserValidator.validate(user));
        assertThat(thrown.getMessage()).isEqualTo("Дата рождения не может быть в будущем.");
    }

    // Тест 6: Успех при создании корректного пользователя
    @Test
    public void testCreateValidUserReturnsCreated() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User result = controller.createUser(user);
        assertThat(result.getId()).isGreaterThan(0);
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getLogin()).isEqualTo("validlogin");
        assertThat(result.getName()).isEqualTo("validlogin");
        assertThat(result.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    // Тест 7: Обновление пользователя с корректными данными
    @Test
    public void testUpdateUserReturnsUpdated() {
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("old@example.com");
        existingUser.setLogin("oldlogin");
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));
        controller.createUser(existingUser);

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("new@example.com");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("newname");
        updatedUser.setBirthday(LocalDate.of(1995, 1, 1));

        User result = controller.updateUser(updatedUser);
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getLogin()).isEqualTo("newlogin");
        assertThat(result.getName()).isEqualTo("newname");
        assertThat(result.getBirthday()).isEqualTo(LocalDate.of(1995, 1, 1));
    }

    // Тест 8: Попытка обновить несуществующего пользователя
    @Test
    public void testUpdateNonExistingUserThrowsError() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999);
        nonExistentUser.setEmail("nonexistent@example.com");
        nonExistentUser.setLogin("nonexistlogin");

        ValidationException thrown = assertThrows(ValidationException.class, () -> controller.updateUser(nonExistentUser));
        assertThat(thrown.getMessage()).isEqualTo("Пользователь с указанным id не найден.");
    }

    // Тест 9: Обновление пользователя с неверными данными
    @Test
    public void testUpdateUserWithInvalidDataThrowsError() {
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("old@example.com");
        existingUser.setLogin("oldlogin");
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));
        controller.createUser(existingUser);

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail(""); // Пустая почта
        updatedUser.setLogin("newlogin");
        updatedUser.setBirthday(LocalDate.of(1995, 1, 1));

        ValidationException thrown = assertThrows(ValidationException.class, () -> controller.updateUser(updatedUser));
        assertThat(thrown.getMessage()).isEqualTo("Электронная почта не может быть пустой и должна содержать символ @.");
    }
}
