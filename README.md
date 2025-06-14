# java-filmorate
Template repository for Filmorate project.

#Схема базы данных учебного проекта java_filmorate

![erd](https://github.com/LikhachevS/java-filmorate/blob/add-database/filmorate_erd.PNG)

На основе предоставленной схемы базы данных можно составить несколько основных запросов, которые позволят выполнять различные операции с данными. Вот примеры таких запросов:

##1. Получение списка всех фильмов
Этот запрос возвращает все фильмы из таблицы film с их рейтингами.

SELECT film.film_id, film.title, film.description, film.release_date, film.duration, rating.rating_name
FROM film
JOIN rating ON film.rating_id = rating.rating_id;

##2. Получение списка фильмов по жанру
Этот запрос возвращает все фильмы определенного жанра.

SELECT film.film_id, film.title, film.description, film.release_date, film.duration, rating.rating_name
FROM film
JOIN film_genre ON film.film_id = film_genre.film_id
JOIN genre ON film_genre.genre_id = genre.genre_id
WHERE genre.genre_name = 'Action';

##3. Получение списка фильмов, которые понравились пользователю
Этот запрос возвращает все фильмы, которые понравились определенному пользователю.

SELECT film.film_id, film.title, film.description, film.release_date, film.duration, rating.rating_name
FROM film
JOIN likes ON film.film_id = likes.film_id
JOIN user ON likes.user_id = user.user_id
WHERE user.user_id = 1;

##4. Получение списка друзей пользователя
Этот запрос возвращает всех друзей определенного пользователя.

SELECT friend.user_id, friend.friend_id, friend.status
FROM friend
WHERE friend.user_id = 1;

##5. Получение списка пользователей, которые родились в определенном году
Этот запрос возвращает всех пользователей, которые родились в определенном году.

SELECT user.user_id, user.name, user.login, user.email, user.birthday
FROM user
WHERE YEAR(user.birthday) = 1990;

##6. Получение списка фильмов с рейтингом выше определенного значения
Этот запрос возвращает все фильмы с рейтингом выше определенного значения.

SELECT film.film_id, film.title, film.description, film.release_date, film.duration, rating.rating_name
FROM film
JOIN rating ON film.rating_id = rating.rating_id
WHERE rating.rating_name > 'PG-13';

##7. Получение списка фильмов, выпущенных в определенном году
Этот запрос возвращает все фильмы, выпущенные в определенном году.

SELECT film.film_id, film.title, film.description, film.release_date, film.duration, rating.rating_name
FROM film
JOIN rating ON film.rating_id = rating.rating_id
WHERE YEAR(film.release_date) = 2020;

##8. Получение списка фильмов, которые понравились друзьям пользователя
Этот запрос возвращает все фильмы, которые понравились друзьям определенного пользователя.

SELECT film.film_id, film.title, film.description, film.release_date, film.duration, rating.rating_name
FROM film
JOIN likes ON film.film_id = likes.film_id
JOIN friend ON likes.user_id = friend.friend_id
WHERE friend.user_id = 1;

Эти запросы покрывают основные операции с данными в базе данных, включая выборку фильмов, пользователей, жанров и рейтингов, а также взаимодействие между ними.