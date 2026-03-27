package com.libraryservice.strategy.movie;

import com.libraryservice.dao.MovieDAO;
import com.libraryservice.model.Movie;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * STRATEGY + ADAPTER.
 * <p>
 * Verantwoordelijkheid:
 * - Ophalen van films via de MovieDAO
 * - Omzetten van ruwe database data naar het interne Movie model
 * <p>
 * Adapter functie:
 * De data uit de database (Map<String, Object>) wordt hier omgezet
 * naar een gestandaardiseerd Movie object.
 * <p>
 * Hierdoor blijft de rest van de applicatie (services en controllers)
 * volledig onafhankelijk van de onderliggende data-opslag.
 */
@Component
public class MovieDbStrategy implements MovieApiStrategy {
    private static final Logger logger = Logger.getLogger(MovieDbStrategy.class.getName());

    private final MovieDAO movieDAO;

    public MovieDbStrategy(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    /**
     * Zoekt films op basis van een zoekterm.
     *
     * @param query      zoekterm
     * @param maxResults maximaal aantal resultaten
     * @return lijst van Movie objecten
     */
    @Override
    public List<Movie> searchMovies(String query, int maxResults) {
        logger.info("MovieDbStrategy: searchMovies query: " + query);
        return movieDAO.searchMovies(query, maxResults).stream()
                .map(this::mapToMovie)
                .collect(Collectors.toList());
    }

    /**
     * Haalt één film op via een unieke identifier.
     *
     * @param movieId unieke ID van de film
     * @return Movie object of null indien niet gevonden
     */
    @Override
    public Movie findById(String movieId) {
        logger.info("MovieDbStrategy: findById " + movieId);
        Map<String, Object> raw = movieDAO.findById(movieId);
        if (raw == null || raw.isEmpty() || !"True".equals(raw.get("Response"))) {
            return null;
        }
        return mapToMovie(raw);
    }

    /**
     * Zet ruwe data om naar een Movie object.
     *
     * @param raw ruwe data uit de database
     * @return gestructureerd Movie object
     */
    private Movie mapToMovie(Map<String, Object> raw) {
        return new Movie(
                (String) raw.get("imdbID"),
                (String) raw.get("Title"),
                (String) raw.get("Plot"),
                (String) raw.get("Rated"),
                (String) raw.get("Released"),
                (String) raw.get("Runtime"),
                (String) raw.get("Year"),
                (String) raw.get("Director"),
                (String) raw.get("Writer"),
                (String) raw.get("Language"),
                (String) raw.get("Poster"),
                (String) raw.get("Actors"),
                (String) raw.get("Genre"),
                (String) raw.get("imdbRating"),
                (String) raw.getOrDefault("Type", "movie")
        );
    }
}