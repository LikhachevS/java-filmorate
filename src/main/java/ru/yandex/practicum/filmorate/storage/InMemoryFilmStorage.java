package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private Integer nextId = 1;

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
        try {
            FilmValidator.validate(film);

            film.setId(nextId++);
            films.put(film.getId(), film);
            log.info("Фильм успешно добавлен: {}", film.getName());
            return film;
        } catch (ValidationException e) {
            log.error("Ошибка при валидации фильма: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        try {
            if (Objects.isNull(film.getId())) {
                throw new ValidationException("Поле id не может быть пустым.");
            }

            if (!films.containsKey(film.getId())) {
                throw new NotFoundException("Фильм с указанным id не найден.");
            }

            Film originalFilm = films.get(film.getId());

            Film tempFilm = new Film();
            tempFilm.setId(film.getId());
            tempFilm.setName((film.getName() != null) ?
                    film.getName() : originalFilm.getName());
            tempFilm.setDescription((film.getDescription() != null) ?
                    film.getDescription() : originalFilm.getDescription());
            tempFilm.setReleaseDate((film.getReleaseDate() != null) ?
                    film.getReleaseDate() : originalFilm.getReleaseDate());
            tempFilm.setDuration((film.getDuration() != null) ?
                    film.getDuration() : originalFilm.getDuration());

            FilmValidator.validate(tempFilm);

            originalFilm.setName(tempFilm.getName());
            originalFilm.setDescription(tempFilm.getDescription());
            originalFilm.setReleaseDate(tempFilm.getReleaseDate());
            originalFilm.setDuration(tempFilm.getDuration());

            log.info("Фильм успешно обновлён: {}", originalFilm.getName());
            return originalFilm;
        } catch (ValidationException | NotFoundException e) {
            log.error("Ошибка при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    public Film getFilmById(Integer id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
        return films.get(id);
    }
}
