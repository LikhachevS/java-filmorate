package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    protected final RowMapper<Genre> mapper;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre> mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String sql = "SELECT * FROM genre ORDER BY id";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public Genre getGenreById(int id) {
        try {
            String sql = "SELECT * FROM genre WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Жанр с id = " + id + " не найден.");
        }
    }
}