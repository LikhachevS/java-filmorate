package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private Integer nextId = 1;

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
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

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        try {
            if (Objects.isNull(updatedFilm.getId())) {
                throw new ValidationException("Поле id не может быть пустым.");
            }

            if (!films.containsKey(updatedFilm.getId())) {
                throw new ValidationException("Фильм с указанным id не найден.");
            }

            Film originalFilm = films.get(updatedFilm.getId());

            Film tempFilm = new Film();
            tempFilm.setId(updatedFilm.getId());
            tempFilm.setName((updatedFilm.getName() != null) ?
                    updatedFilm.getName() : originalFilm.getName());
            tempFilm.setDescription((updatedFilm.getDescription() != null) ?
                    updatedFilm.getDescription() : originalFilm.getDescription());
            tempFilm.setReleaseDate((updatedFilm.getReleaseDate() != null) ?
                    updatedFilm.getReleaseDate() : originalFilm.getReleaseDate());
            tempFilm.setDuration((updatedFilm.getDuration() != null) ?
                    updatedFilm.getDuration() : originalFilm.getDuration());

            FilmValidator.validate(tempFilm);

            originalFilm.setName(tempFilm.getName());
            originalFilm.setDescription(tempFilm.getDescription());
            originalFilm.setReleaseDate(tempFilm.getReleaseDate());
            originalFilm.setDuration(tempFilm.getDuration());

            log.info("Фильм успешно обновлён: {}", originalFilm.getName());
            return originalFilm;
        } catch (ValidationException e) {
            log.error("Ошибка при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }
}