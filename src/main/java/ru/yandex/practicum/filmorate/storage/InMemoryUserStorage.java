package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private Integer nextId = 1;

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User createUser(User user) {
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

    @Override
    public User updateUser(User user) {
        try {
            if (Objects.isNull(user.getId())) {
                throw new ValidationException("Поле id не может быть пустым.");
            }
            if (!users.containsKey(user.getId())) {
                throw new NotFoundException("Пользователь с указанным id не найден.");
            }

            User originalUser = users.get(user.getId());

            User tempUser = new User();
            tempUser.setId(user.getId());
            tempUser.setEmail((user.getEmail() != null) ? user.getEmail() : originalUser.getEmail());
            tempUser.setLogin((user.getLogin() != null) ? user.getLogin() : originalUser.getLogin());
            tempUser.setName((user.getName() != null) ? user.getName() : originalUser.getName());
            tempUser.setBirthday((user.getBirthday() != null) ? user.getBirthday() : originalUser.getBirthday());

            UserValidator.validate(tempUser);

            originalUser.setEmail(tempUser.getEmail());
            originalUser.setLogin(tempUser.getLogin());
            originalUser.setName(tempUser.getName());
            originalUser.setBirthday(tempUser.getBirthday());

            log.info("Пользователь успешно обновлён: {}", originalUser.getLogin());
            return originalUser;
        } catch (ValidationException | NotFoundException e) {
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    public User getUserById(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
        return users.get(id);
    }
}
