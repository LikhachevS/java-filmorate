package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void addFriend(Integer id1, Integer id2) {
        try {
            User user1 = userStorage.getUserById(id1);
            User user2 = userStorage.getUserById(id2);
            user1.getFriends().add(user2.getId());
            user2.getFriends().add(user1.getId());
        } catch (NotFoundException e) {
            log.error("Ошибка при добавлении в друзья: {}", e.getMessage());
            throw e;
        }
    }

    public void removeFriend(Integer id1, Integer id2) {
        try {
            User user1 = userStorage.getUserById(id1);
            User user2 = userStorage.getUserById(id2);
            user1.getFriends().remove(user2.getId());
            user2.getFriends().remove(user1.getId());
        } catch (NotFoundException e) {
            log.error("Ошибка при удалении из друзей: {}", e.getMessage());
            throw e;
        }
    }

    public Set<User> getCommonFriends(Integer id1, Integer id2) {
        try {
            User user1 = userStorage.getUserById(id1);
            User user2 = userStorage.getUserById(id2);

            Set<Integer> commonFriendIds = new HashSet<>(user1.getFriends());
            commonFriendIds.retainAll(user2.getFriends());
            Set<User> commonFriends = new HashSet<>();
            for (Integer friendId : commonFriendIds) {
                commonFriends.add(userStorage.getUserById(friendId));
            }
            return commonFriends;
        } catch (NotFoundException e) {
            log.error("Ошибка при получении списка общих друзей: {}", e.getMessage());
            throw e;
        }
    }

    public Set<User> getFriends(Integer id) {
        try {
            User user = userStorage.getUserById(id);

            Set<Integer> friendIds = new HashSet<>(user.getFriends());
            Set<User> friends = new HashSet<>();
            for (Integer friendId : friendIds) {
                friends.add(userStorage.getUserById(friendId));
            }
            return friends;
        } catch (NotFoundException e) {
            log.error("Ошибка при получении списка друзей: {}", e.getMessage());
            throw e;
        }
    }
}
