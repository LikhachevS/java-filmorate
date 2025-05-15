package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film updatedFilm) {
        return filmStorage.updateFilm(updatedFilm);
    }

    public void addLike(Integer filmId, Integer userId) {
        try {
            Film film = filmStorage.getFilmById(filmId);
            userStorage.getUserById(userId);
            film.getLikes().add(userId);
        } catch (NotFoundException e) {
            log.error("Ошибка при добавлении лайка: {}", e.getMessage());
            throw e;
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        try {
            Film film = filmStorage.getFilmById(filmId);
            userStorage.getUserById(userId);
            film.getLikes().remove(userId);
        } catch (NotFoundException e) {
            log.error("Ошибка при удалении лайка: {}", e.getMessage());
            throw e;
        }
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
