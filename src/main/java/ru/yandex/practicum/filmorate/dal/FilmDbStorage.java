package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    protected final RowMapper<Film> mapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        Collection<Film> films = jdbcTemplate.query(sql, mapper);
        films.forEach(film -> {
            loadFilmGenres(film);
            loadFilmLikes(film);
        });
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        try {
            FilmValidator.validate(film);

            String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " + "VALUES (?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getMpa().getId());
                return ps;
            }, keyHolder);

            int filmId = keyHolder.getKey().intValue();
            film.setId(filmId);
            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                updateFilmGenres(film);
            }
            log.info("Фильм успешно добавлен: {}", film.getName());
            return getFilmById(filmId);
        } catch (ValidationException | NotFoundException e) {
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

            Film originalFilm = getFilmById(film.getId());

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
            tempFilm.setGenres((film.getGenres() != null) ?
                    film.getGenres() : originalFilm.getGenres());
            tempFilm.setMpa((film.getMpa() != null) ?
                    film.getMpa() : originalFilm.getMpa());
            tempFilm.setLikes(originalFilm.getLikes());

            FilmValidator.validate(tempFilm);

            String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? "
                    + "WHERE film_id = ?";
            jdbcTemplate.update(sql, tempFilm.getName(), tempFilm.getDescription(), tempFilm.getReleaseDate(),
                    tempFilm.getDuration(), ((tempFilm.getMpa() != null) ? tempFilm.getMpa().getId() : null), tempFilm.getId());

            jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", tempFilm.getId());
            if (tempFilm.getGenres() != null && !tempFilm.getGenres().isEmpty()) {
                updateFilmGenres(tempFilm);
            }

            log.info("Фильм успешно обновлён: {}", tempFilm.getName());
            return tempFilm;
        } catch (ValidationException | NotFoundException e) {
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Film getFilmById(Integer id) {
        try {
            String sql = "SELECT * FROM films WHERE film_id = ?";
            Film film = jdbcTemplate.queryForObject(sql, mapper, id);

            assert film != null;
            loadFilmGenres(film);
            loadFilmLikes(film);

            return film;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private void updateFilmGenres(Film film) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = ?";
        List<Integer> genresId = jdbcTemplate.queryForList(sql, Integer.class, film.getId());
        Set<Genre> genres = genresId.stream()
                .map(Genre::new)
                .collect(Collectors.toSet());
        film.setGenres(genres);
    }

    private void loadFilmLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sql, Integer.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }
}