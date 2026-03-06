package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    protected final RowMapper<Film> mapper;

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

            String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                    "VALUES (:name, :description, :release_date, :duration, :mpa_id)";

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", film.getName())
                    .addValue("description", film.getDescription())
                    .addValue("release_date", film.getReleaseDate())
                    .addValue("duration", film.getDuration())
                    .addValue("mpa_id", film.getMpa().getId());


            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, params, keyHolder);

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

            String sql = "UPDATE films SET name = :name, description = :description, release_date = :release_date, duration = :duration, mpa_id = :mpa_id "
                    + "WHERE film_id = :film_id";

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", tempFilm.getName())
                    .addValue("description", tempFilm.getDescription())
                    .addValue("release_date", tempFilm.getReleaseDate())
                    .addValue("duration", tempFilm.getDuration())
                    .addValue("mpa_id", (tempFilm.getMpa() != null) ? tempFilm.getMpa().getId() : null)
                    .addValue("film_id", tempFilm.getId());

            jdbcTemplate.update(sql, params);

            jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = :film_id",
                    new MapSqlParameterSource("film_id", tempFilm.getId()));
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
            String sql = "SELECT * FROM films WHERE film_id = :film_id";
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("film_id", id);
            Film film = jdbcTemplate.queryForObject(sql, params, mapper);

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
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (:film_id, :user_id)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", filmId)
                .addValue("user_id", userId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = :film_id AND user_id = :user_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", filmId)
                .addValue("user_id", userId);
        jdbcTemplate.update(sql, params);
    }

    private void updateFilmGenres(Film film) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (:film_id, :genre_id)";

        for (Genre genre : film.getGenres()) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("film_id", film.getId())
                    .addValue("genre_id", genre.getId());
            jdbcTemplate.update(sql, params);
        }
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", film.getId());
        List<Integer> genresId = jdbcTemplate.queryForList(sql, params, Integer.class);
        Set<Genre> genres = genresId.stream()
                .map(Genre::new)
                .collect(Collectors.toSet());
        film.setGenres(genres);
    }

    private void loadFilmLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", film.getId());
        List<Integer> likes = jdbcTemplate.queryForList(sql, params, Integer.class);
        film.setLikes(new HashSet<>(likes));

    }
}