package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Genre {
    private int id;
    private String name;

    public Genre() {
    }

    public Genre(int id) {
        this.id = id;
    }
}