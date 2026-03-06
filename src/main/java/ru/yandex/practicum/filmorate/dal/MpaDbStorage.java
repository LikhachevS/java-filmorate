package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    protected final RowMapper<Mpa> mapper;

    @Override
    public Collection<Mpa> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public Mpa getMpaById(int id) {
        try {
            String sql = "SELECT * FROM mpa WHERE id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", id);
            return jdbcTemplate.queryForObject(sql, params, mapper);

        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Рейтинг с id = " + id + " не найден.");
        }
    }
}