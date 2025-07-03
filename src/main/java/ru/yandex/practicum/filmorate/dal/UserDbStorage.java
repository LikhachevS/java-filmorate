package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = :user_id";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (:email, :login, :name, :birthday)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = :email, login = :login, name = :name, " +
            "birthday = :birthday WHERE user_id = :user_id";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    protected final RowMapper<User> mapper;

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public User createUser(User user) {
        try {
            UserValidator.validate(user);

            KeyHolder keyHolder = new GeneratedKeyHolder();

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("email", user.getEmail())
                    .addValue("login", user.getLogin())
                    .addValue("name", user.getName())
                    .addValue("birthday", user.getBirthday());

            jdbcTemplate.update(INSERT_QUERY, params, keyHolder);

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

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("email", tempUser.getEmail())
                    .addValue("login", tempUser.getLogin())
                    .addValue("name", tempUser.getName())
                    .addValue("birthday", tempUser.getBirthday())
                    .addValue("user_id", tempUser.getId());

            jdbcTemplate.update(UPDATE_QUERY, params);

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
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("user_id", id);

            return jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, params, mapper);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        try {
            getUserById(userId);
            getUserById(friendId);
            String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (:user_id, :friend_id)";
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("user_id", userId)
                    .addValue("friend_id", friendId);
            jdbcTemplate.update(sql, params);
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
            String sql = "DELETE FROM friendship WHERE user_id = :user_id AND friend_id = :friend_id";
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("user_id", userId)
                    .addValue("friend_id", friendId);
            jdbcTemplate.update(sql, params);
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
            String sql = "SELECT friend_id FROM friendship WHERE user_id = :user_id";
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("user_id", userId);
            List<Integer> friendIds = jdbcTemplate.queryForList(sql, params, Integer.class);

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