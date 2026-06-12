DROP PROCEDURE IF EXISTS add_movie;
DROP PROCEDURE IF EXISTS get_duplicate_movie_id;
DROP PROCEDURE IF EXISTS get_next_movie_id;
DROP PROCEDURE IF EXISTS get_next_star_id;
DROP PROCEDURE IF EXISTS get_next_genre_id;

DELIMITER $$

CREATE PROCEDURE add_movie (
    IN title VARCHAR(100),
    IN year INTEGER,
    IN director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
)
add_movie: BEGIN
    DECLARE duplicate_movie_id VARCHAR(10);
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INTEGER;
    DECLARE price DECIMAL(10, 2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1
            @sqlstate = RETURNED_SQLSTATE,
            @errno = MYSQL_ERRNO,
            @text = MESSAGE_TEXT;

        SELECT "ROLLED_BACK" AS message, CONCAT("ERROR ", @errno, " (", @sqlstate, "): ", @text) AS error;
        ROLLBACK;
    END;

    CALL get_duplicate_movie_id(title, year, director, duplicate_movie_id);

    IF duplicate_movie_id IS NOT NULL THEN
        SELECT "DUPLICATE_MOVIE" AS message;
        LEAVE add_movie;
    END IF;

    START TRANSACTION;

    CALL get_next_movie_id(movie_id);
    CALL get_price(price);

    INSERT INTO movies (id, title, year, director, price) VALUES (movie_id, title, year, director, price);

    IF star_name IS NOT NULL THEN
        SET star_id = NULL;
        SELECT id INTO star_id FROM stars WHERE name = star_name LIMIT 1;

        IF star_id IS NULL THEN
           CALL get_next_star_id(star_id);
           INSERT INTO stars (id, name, birthYear) VALUES ( star_id, star_name, NULL);
        END IF;

        INSERT INTO stars_in_movies(starId, movieId) VALUES (star_id, movie_id);
    END IF;

    IF genre_name IS NOT NULL THEN
        SET genre_id = NULL;
        SELECT id INTO genre_id FROM genres WHERE name = genre_name LIMIT 1;

        IF genre_id IS NULL THEN
           CALL get_next_genre_id(genre_id);
           INSERT INTO genres (id, name) VALUES (genre_id, genre_name);
        END IF;

        INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
    END IF;

    COMMIT;

    SELECT "SUCCESS" AS message, movie_id AS movieId, star_id AS starId, genre_id AS genreId;
END add_movie$$

CREATE PROCEDURE get_duplicate_movie_id(
    IN _title VARCHAR(100),
    IN _year INTEGER,
    IN _director VARCHAR(100),
    OUT duplicate_movie_id VARCHAR(10)
)
BEGIN
    SELECT id INTO duplicate_movie_id
    FROM movies
    WHERE title = _title
    AND year = _year
    AND director = _director
    LIMIT 1;
END$$

CREATE PROCEDURE get_next_movie_id(OUT movie_id VARCHAR(10))
BEGIN
    DECLARE max_id VARCHAR(10);
    DECLARE prefix VARCHAR(10);
    DECLARE postfix VARCHAR(10);
    DECLARE number INTEGER;

    SELECT MAX(id) INTO max_id FROM movies;

    SET prefix = REGEXP_REPLACE(max_id, "[0-9]", "");
    SET postfix = REGEXP_REPLACE(max_id, "[^0-9]", "");
    SET number = CAST(postfix AS UNSIGNED) + 1;

    SET movie_id = CONCAT(prefix, LPAD(number, LENGTH(postfix), "0"));
END$$

CREATE PROCEDURE get_price(OUT price DECIMAL(10, 2))
BEGIN
    SET price = FLOOR(1 + RAND() * 30) + ELT(FLOOR(1 + RAND() * 3), 0.99, 0.49, 0.00);
END$$

CREATE PROCEDURE get_next_star_id(OUT star_id VARCHAR(10))
BEGIN
    DECLARE max_id VARCHAR(10);
    DECLARE prefix VARCHAR(10);
    DECLARE postfix VARCHAR(10);
    DECLARE number INTEGER;

    SELECT MAX(id) INTO max_id FROM stars;

    SET prefix = REGEXP_REPLACE(max_id, "[0-9]", "");
    SET postfix = REGEXP_REPLACE(max_id, "[^0-9]", "");
    SET number = CAST(postfix AS UNSIGNED) + 1;

    SET star_id = CONCAT(prefix, LPAD(number, LENGTH(postfix), "0"));
END$$

CREATE PROCEDURE get_next_genre_id(OUT genre_id INTEGER)
BEGIN
    DECLARE max_id INTEGER;

    SELECT MAX(id) INTO max_id FROM genres;

    SET genre_id = max_id + 1;
END$$

DELIMITER ;