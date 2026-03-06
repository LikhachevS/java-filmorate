package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        film.setMpa(mpaStorage.getMpaById(film.getMpa().getId()));

        Set<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));

        for (Genre genre : film.getGenres()) {
            genres.add(genreStorage.getGenreById(genre.getId()));
        }

        film.setGenres(genres);
        return film;
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
            User user = userStorage.getUserById(userId);
            if (film.getLikes().contains(userId)) {
                throw new ValidationException("Лайк уже поставлен");
            }
            filmStorage.addLike(filmId, userId);
            film.getLikes().add(userId);
        } catch (NotFoundException | ValidationException e) {
            log.error("Ошибка при добавлении лайка: {}", e.getMessage());
            throw e;
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        try {
            Film film = filmStorage.getFilmById(filmId);
            User user = userStorage.getUserById(userId);
            if (!film.getLikes().contains(userId)) {
                throw new ValidationException("Такого лайка нет");
            }
            filmStorage.removeLike(filmId, userId);
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