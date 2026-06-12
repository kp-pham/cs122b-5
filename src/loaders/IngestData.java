package loaders;

import java.sql.Connection;
import java.sql.DriverManager;

public class IngestData {
    public static void main(String[] args) throws Exception {
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        String url = System.getenv("DB_URL") + "?allowLoadLocalInfile=true";

        Class.forName(System.getenv("JDBC_DRIVER"));
        Connection conn = DriverManager.getConnection(url, username, password);

        loadMovies(conn);
        loadStars(conn);
        loadGenres(conn);
        loadStarsInMovies(conn);
        loadGenresInMovies(conn);

        conn.close();
    }

    private static void loadMovies(Connection conn) throws Exception {
        System.out.println("Loading movies...");

        MovieLoader movieLoader = new MovieLoader(conn);
        movieLoader.load("data/movies.csv");

        System.out.println("\nLoading movies completed.\n");
    }

    private static void loadStars(Connection conn) throws Exception {
        System.out.println("Loading stars...");

        StarLoader starLoader = new StarLoader(conn);
        starLoader.load("data/stars.csv");

        System.out.println("\nLoading stars completed.\n");
    }

    private static void loadGenres(Connection conn) throws Exception {
        System.out.println("Loading genres...");

        GenreLoader genreLoader = new GenreLoader(conn);
        genreLoader.load("data/genres.csv");

        System.out.println("\nLoading genres completed.\n");
    }

    private static void loadStarsInMovies(Connection conn) throws Exception {
        System.out.println("\nLoading movie-star relationships...");

        StarsInMoviesLoader starsInMoviesLoader = new StarsInMoviesLoader(conn);
        starsInMoviesLoader.load("data/stars_in_movies.csv");

        System.out.println("\nLoading movie-star relationships completed.\n");
    }

    private static void loadGenresInMovies(Connection conn) throws Exception {
        System.out.println("Loading movie-genre relationships...");

        GenresInMoviesLoader genresInMoviesLoader = new GenresInMoviesLoader(conn);
        genresInMoviesLoader.load("data/genres_in_movies.csv");

        System.out.println("\nLoading movie-genre relationships completed.\n");
    }
}
