package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class, GenreDbStorage.class, GenreRowMapper.class})
class FilmorateApplicationTests {

    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MpaDbStorage mpaStorage;
    @Autowired
    private GenreDbStorage genreStorage;

    private Film testFilm1;
    private Film testFilm2;
    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;");

        testFilm1 = new Film();
        testFilm1.setName("Film 1");
        testFilm1.setDescription("This is a test film 1");
        testFilm1.setReleaseDate(LocalDate.of(1990, 9, 9));
        testFilm1.setDuration(100);
        testFilm1.setMpa(new Mpa(1, "G"));
        testFilm1.setGenres(Set.of(new Genre(1, "Комедия")));

        testFilm2 = new Film();
        testFilm2.setName("Film 2");
        testFilm2.setDescription("This is a test film 2");
        testFilm2.setReleaseDate(LocalDate.of(1990, 9, 9));
        testFilm2.setDuration(120);
        testFilm2.setMpa(new Mpa(2, "PG"));
        testFilm2.setGenres(Set.of(new Genre(2, "Драма")));

        testUser1 = new User();
        testUser1.setLogin("User1");
        testUser1.setName("User 1");
        testUser1.setEmail("@User1");
        testUser1.setBirthday(LocalDate.of(1998, 6, 2));

        testUser2 = new User();
        testUser2.setLogin("User2");
        testUser2.setName("User 2");
        testUser2.setEmail("@User2");
        testUser2.setBirthday(LocalDate.of(1999, 7, 3));

        testUser3 = new User();
        testUser3.setLogin("User3");
        testUser3.setName("User 3");
        testUser3.setEmail("@User3");
        testUser3.setBirthday(LocalDate.of(2000, 8, 4));
    }

    //Тесты для filmStorage
    @Test
    void testAddFilm() {
        Film addedFilm = filmStorage.addFilm(testFilm1);

        assertNotNull(addedFilm);
        assertNotNull(addedFilm.getId());
        assertEquals("Film 1", addedFilm.getName());
        assertEquals("This is a test film 1", addedFilm.getDescription());
    }

    @Test
    void testUpdateFilm() {
        filmStorage.addFilm(testFilm1);
        testFilm2.setId(1);
        Film updatedFilm = filmStorage.updateFilm(testFilm2);

        // Проверяем, что фильм обновлен
        assertNotNull(updatedFilm);
        assertEquals("Film 2", updatedFilm.getName());
        assertEquals("This is a test film 2", updatedFilm.getDescription());
    }

    @Test
    void testGetAllFilms() {
        Film addedFilm1 = filmStorage.addFilm(testFilm1);
        Film addedFilm2 = filmStorage.addFilm(testFilm2);

        Collection<Film> films = filmStorage.getAllFilms();

        assertNotNull(films);
        assertEquals(2, films.size());
        assertTrue(films.contains(addedFilm1));
        assertTrue(films.contains(addedFilm2));
    }

    @Test
    void testGetFilmById() {
        filmStorage.addFilm(testFilm1);
        Film retrievedFilm = filmStorage.getFilmById(testFilm1.getId());

        assertNotNull(retrievedFilm);
        assertEquals(testFilm1.getId(), retrievedFilm.getId());
        assertEquals(testFilm1.getName(), retrievedFilm.getName());
        assertEquals(testFilm1.getDescription(), retrievedFilm.getDescription());
    }

    @Test
    void testAddAndDelLike() {
        User user1 = new User();
        user1.setLogin("User");
        user1.setName("User 1");
        user1.setEmail("@User1");
        user1.setBirthday(LocalDate.of(1998, 6, 2));

        userStorage.createUser(user1);
        filmStorage.addFilm(testFilm1);
        filmStorage.addLike(1, 1);

        Film retrievedFilm = filmStorage.getFilmById(testFilm1.getId());

        assertNotNull(retrievedFilm);
        assertTrue(retrievedFilm.getLikes().contains(1));

        filmStorage.removeLike(1, 1);
        Film retrievedFilm1 = filmStorage.getFilmById(testFilm1.getId());
        assertNotNull(retrievedFilm);
        assertFalse(retrievedFilm1.getLikes().contains(1));
    }

    //Тесты для UserStorage
    @Test
    void testCreateUser() {
        User addedUser = userStorage.createUser(testUser1);

        assertNotNull(addedUser);
        assertNotNull(addedUser.getId());
        assertEquals("User 1", addedUser.getName());
        assertEquals("User1", addedUser.getLogin());
        assertEquals("@User1", addedUser.getEmail());
        assertEquals(LocalDate.of(1998, 6, 2), addedUser.getBirthday());
    }

    @Test
    void testGetAllUsers() {
        User addedUser1 = userStorage.createUser(testUser1);
        User addedUser2 = userStorage.createUser(testUser2);

        Collection<User> users = userStorage.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.contains(addedUser1));
        assertTrue(users.contains(addedUser2));
    }

    @Test
    void testUpdateUser() {
        userStorage.createUser(testUser1);
        testUser2.setId(1);
        User updatedUser = userStorage.updateUser(testUser2);

        assertNotNull(updatedUser);
        assertEquals("User 2", updatedUser.getName());
        assertEquals("User2", updatedUser.getLogin());
        assertEquals("@User2", updatedUser.getEmail());
        assertEquals(LocalDate.of(1999, 7, 3), updatedUser.getBirthday());
    }

    @Test
    void testAddAndRemoveFriend() {
        User addedUser1 = userStorage.createUser(testUser1);
        User addedUser2 = userStorage.createUser(testUser2);

        userStorage.addFriend(addedUser1.getId(), addedUser2.getId());

        Set<User> friends = userStorage.getFriends(addedUser1.getId());
        assertNotNull(friends);
        assertTrue(friends.contains(addedUser2));

        userStorage.removeFriend(addedUser1.getId(), addedUser2.getId());

        friends = userStorage.getFriends(addedUser1.getId());
        assertNotNull(friends);
        assertFalse(friends.contains(addedUser2));
    }

    @Test
    void testGetCommonFriends() {

        User addedUser1 = userStorage.createUser(testUser1);
        User addedUser2 = userStorage.createUser(testUser2);
        User addedUser3 = userStorage.createUser(testUser3);

        // Добавляем друзей
        userStorage.addFriend(addedUser1.getId(), addedUser3.getId());
        userStorage.addFriend(addedUser2.getId(), addedUser3.getId());

        // Получаем общих друзей
        Set<User> commonFriends = userStorage.getCommonFriends(addedUser1.getId(), addedUser2.getId());

        // Проверяем, что общие друзья найдены
        assertNotNull(commonFriends);
        assertTrue(commonFriends.contains(addedUser3));
    }

    @Test
    void testGetFriends() {
        User addedUser1 = userStorage.createUser(testUser1);
        User addedUser2 = userStorage.createUser(testUser2);
        User addedUser3 = userStorage.createUser(testUser3);

        userStorage.addFriend(addedUser1.getId(), addedUser2.getId());
        userStorage.addFriend(addedUser1.getId(), addedUser3.getId());

        Set<User> friends = userStorage.getFriends(addedUser1.getId());

        assertNotNull(friends);
        assertTrue(friends.contains(addedUser2));
        assertTrue(friends.contains(addedUser3));
    }

    //Тесты для MpaStorage
    @Test
    void testGetAllMpaRatings() {
        Collection<Mpa> mpaRatings = mpaStorage.getAllMpaRatings();
        assertEquals(5, mpaRatings.size());
    }

    @Test
    void testGetMpaById() {
        Mpa mpa = mpaStorage.getMpaById(2);
        assertEquals("PG", mpa.getName());
    }

    @Test
    void testGetNonExistingMpa() {
        assertThrows(NotFoundException.class, () -> {
            mpaStorage.getMpaById(999);
            ;
        });
    }

    //Тесты для GenreStorage
    @Test
    void testGetAllGenres() {
        Collection<Genre> genres = genreStorage.getAllGenres();
        assertEquals(6, genres.size());
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreStorage.getGenreById(2);
        assertEquals("Драма", genre.getName());
    }

    @Test
    void testGetNonExistingGenre() {
        assertThrows(NotFoundException.class, () -> {
            genreStorage.getGenreById(999);
            ;
        });
    }
}