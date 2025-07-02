package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    protected final RowMapper<Mpa> mapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Mpa> mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public Collection<Mpa> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public Mpa getMpaById(int id) {
        try {
            String sql = "SELECT * FROM mpa WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Рейтинг с id = " + id + " не найден.");
        }
    }
}