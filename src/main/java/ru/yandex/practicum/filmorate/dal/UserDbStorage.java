package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Repository
@Primary
public class UserDbStorage implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate;
    protected final RowMapper<User> mapper;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public User createUser(User user) {
        try {
            UserValidator.validate(user);

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"user_id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);

            int userId = keyHolder.getKey().intValue();
            user.setId(userId);
            log.info("Пользователь успешно создан: {}", user.getLogin());
            return getUserById(userId);
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

            User originalUser = getUserById(user.getId());

            User tempUser = new User();
            tempUser.setId(user.getId());
            tempUser.setEmail((user.getEmail() != null) ? user.getEmail() : originalUser.getEmail());
            tempUser.setLogin((user.getLogin() != null) ? user.getLogin() : originalUser.getLogin());
            tempUser.setName((user.getName() != null) ? user.getName() : originalUser.getName());
            tempUser.setBirthday((user.getBirthday() != null) ? user.getBirthday() : originalUser.getBirthday());

            UserValidator.validate(tempUser);

            jdbcTemplate.update(UPDATE_QUERY, tempUser.getEmail(), tempUser.getLogin(), tempUser.getName()
                    , tempUser.getBirthday(), tempUser.getId());

            log.info("Пользователь успешно обновлён: {}", tempUser.getLogin());
            return tempUser;
        } catch (ValidationException | NotFoundException e) {
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public User getUserById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        try {
            getUserById(userId);
            getUserById(friendId);
            String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, userId, friendId);
            log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        } catch (NotFoundException e) {
            log.error("Ошибка при добавлении в друзья: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        try {
            getUserById(userId);
            getUserById(friendId);
            String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(sql, userId, friendId);
            log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        } catch (NotFoundException e) {
            log.error("Ошибка при удалении из друзей: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Set<User> getCommonFriends(Integer userId1, Integer userId2) {
        try {
            getUserById(userId1);
            getUserById(userId2);

            Set<User> commonFriends = new HashSet<>(getFriends(userId1));
            commonFriends.retainAll(getFriends(userId2));
            return commonFriends;
        } catch (NotFoundException e) {
            log.error("Ошибка при получении списка общих друзей: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Set<User> getFriends(Integer userId) {
        try {
            getUserById(userId);
            String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
            List<Integer> friendIds = jdbcTemplate.queryForList(sql, Integer.class, userId);

            Set<User> friends = new HashSet<>();
            for (Integer friendId : friendIds) {
                friends.add(getUserById(friendId));
            }
            return friends;
        } catch (NotFoundException e) {
            log.error("Ошибка при получении списка друзей: {}", e.getMessage());
            throw e;
        }
    }
}