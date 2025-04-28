package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    private Integer nextId = 1;

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        try {
            UserValidator.validate(user);

            user.setId(nextId++);
            users.put(user.getId(), user);
            log.info("Пользователь успешно создан: {}", user.getLogin());
            return user;
        } catch (ValidationException e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        try {
            if (Objects.isNull(updatedUser.getId())) {
                throw new ValidationException("Поле id не может быть пустым.");
            }
            if (!users.containsKey(updatedUser.getId())) {
                throw new ValidationException("Пользователь с указанным id не найден.");
            }

            User originalUser = users.get(updatedUser.getId());

            User tempUser = new User();
            tempUser.setId(updatedUser.getId());
            tempUser.setEmail((updatedUser.getEmail() != null) ? updatedUser.getEmail() : originalUser.getEmail());
            tempUser.setLogin((updatedUser.getLogin() != null) ? updatedUser.getLogin() : originalUser.getLogin());
            tempUser.setName((updatedUser.getName() != null) ? updatedUser.getName() : originalUser.getName());
            tempUser.setBirthday((updatedUser.getBirthday() != null) ? updatedUser.getBirthday() : originalUser.getBirthday());

            UserValidator.validate(tempUser);

            originalUser.setEmail(tempUser.getEmail());
            originalUser.setLogin(tempUser.getLogin());
            originalUser.setName(tempUser.getName());
            originalUser.setBirthday(tempUser.getBirthday());

            log.info("Пользователь успешно обновлён: {}", originalUser.getLogin());
            return originalUser;
        } catch (ValidationException e) {
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }
}