package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private FilmController controller;
    private FilmStorage filmStorage;
    private FilmService filmService;
    private UserStorage userStorage;

    @BeforeEach
    public void setup() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        controller = new FilmController(filmService);
    }

    // Тест 1: Валидация не прошла (пустое название)
    @Test
    public void testAddFilmWithEmptyTitleFailsValidation() {
        Film film = new Film();
        film.setName("");
        ValidationException thrown = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertThat(thrown.getMessage()).isEqualTo("Название фильма не может быть пустым.");
    }

    // Тест 2: Валидация не прошла (слишком длинное описание)
    @Test
    public void testAddFilmWithTooLongDescriptionFailsValidation() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("a".repeat(201));
        ValidationException thrown = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertThat(thrown.getMessage()).isEqualTo("Максимальная длина описания — 200 символов.");
    }

    // Тест 3: Валидация не прошла (слишком ранняя дата релиза)
    @Test
    public void testAddFilmWithEarlyReleaseDateFailsValidation() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        ValidationException thrown = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertThat(thrown.getMessage()).isEqualTo("Дата релиза не может быть раньше 28 декабря 1895 года.");
    }

    // Тест 4: Валидация не прошла (отрицательная продолжительность)
    @Test
    public void testAddFilmWithNegativeDurationFailsValidation() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDuration(-10);
        ValidationException thrown = assertThrows(ValidationException.class, () -> FilmValidator.validate(film));
        assertThat(thrown.getMessage()).isEqualTo("Продолжительность фильма должна быть положительной.");
    }

    // Тест 5: Успех при добавлении корректного фильма
    @Test
    public void testAddValidFilmReturnsCreated() {
        Film film = new Film();
        film.setName("Interstellar");
        film.setDescription("Краткое описание фильма");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

        Film result = controller.addFilm(film);
        assertThat(result.getId()).isGreaterThan(0);
        assertThat(result.getName()).isEqualTo("Interstellar");
        assertThat(result.getDescription()).isEqualTo("Краткое описание фильма");
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.of(2014, 11, 7));
        assertThat(result.getDuration()).isEqualTo(169);
    }

    // Тест 6: Обновление фильма с корректными данными
    @Test
    public void testUpdateFilmReturnsUpdated() {
        Film existingFilm = new Film();
        existingFilm.setName("Old Title");
        existingFilm.setDescription("Old Description");
        existingFilm.setReleaseDate(LocalDate.of(2014, 11, 7));
        existingFilm.setDuration(169);
        controller.addFilm(existingFilm);

        Film updatedFilm = new Film();
        updatedFilm.setId(1);
        updatedFilm.setName("New Title");
        updatedFilm.setDescription("New Description");
        updatedFilm.setReleaseDate(LocalDate.of(2015, 11, 7));
        updatedFilm.setDuration(170);

        Film result = controller.updateFilm(updatedFilm);
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("New Title");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.of(2015, 11, 7));
        assertThat(result.getDuration()).isEqualTo(170);
    }

    // Тест 7: Попытка обновить несуществующий фильм
    @Test
    public void testUpdateNonExistingFilmThrowsError() {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999);
        nonExistentFilm.setName("Non Existing Film");

        ValidationException thrown = assertThrows(ValidationException.class, () -> controller.updateFilm(nonExistentFilm));
        assertThat(thrown.getMessage()).isEqualTo("Фильм с указанным id не найден.");
    }

    // Тест 8: Обновление фильма с неверными данными
    @Test
    public void testUpdateFilmWithInvalidDataThrowsError() {
        Film existingFilm = new Film();
        existingFilm.setId(1);
        existingFilm.setName("Old Title");
        existingFilm.setDescription("Old Description");
        existingFilm.setReleaseDate(LocalDate.of(2014, 11, 7));
        existingFilm.setDuration(169);
        controller.addFilm(existingFilm);

        Film updatedFilm = new Film();
        updatedFilm.setId(1);
        updatedFilm.setName(""); // Пустое название
        updatedFilm.setDescription("New Description");
        updatedFilm.setReleaseDate(LocalDate.of(2015, 11, 7));
        updatedFilm.setDuration(170);

        ValidationException thrown = assertThrows(ValidationException.class, () -> controller.updateFilm(updatedFilm));
        assertThat(thrown.getMessage()).isEqualTo("Название фильма не может быть пустым.");
    }
}
