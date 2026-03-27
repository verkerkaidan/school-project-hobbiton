package com.libraryservice.service;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.dto.movie.MovieResponse;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Movie;
import com.libraryservice.strategy.movie.MovieApiStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service verantwoordelijk voor het afhandelen van film-gerelateerde zoekopdrachten.
 * Deze service implementeert de {@link SearchableService} interface en maakt gebruik van
 * een specifieke API-strategie om filmgegevens op te halen en te transformeren naar DTO's.
 */
@Service
public class MovieService implements SearchableService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class.getName());
    private final MovieApiStrategy movieApiStrategy;

    /**
     * Constructor voor MovieService.
     * * @param movieApiStrategy De strategie die gebruikt wordt voor interactie met de externe film API.
     */
    public MovieService(MovieApiStrategy movieApiStrategy) {
        this.movieApiStrategy = movieApiStrategy;
    }

    /**
     * Geeft het type zoekopdracht terug waar deze service verantwoordelijk voor is.
     * * @return Het {@link SearchType#MOVIE} enum type.
     */
    @Override
    public SearchType getSearchType() {
        return SearchType.MOVIE;
    }

    /**
     * Zoekt naar films op basis van de opgegeven zoekparameters.
     * De resultaten worden opgehaald via de strategie en vervolgens gemapt naar {@link MovieResponse} objecten.
     * * @param request Het object met de zoekquery en limieten.
     *
     * @return Een lijst van {@link SearchResult} objecten die voldoen aan de criteria.
     */
    @Override
    public List<SearchResult> search(SearchRequest request) {
        logger.debug("Zoeken naar films met query: {}", request.getQuery());
        List<Movie> movies = movieApiStrategy.searchMovies(request.getQuery(), request.getMaxResults());

        return movies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Zoekt een specifieke film op basis van een uniek identificatienummer.
     * * @param id Het unieke ID van de film (bijv. IMDb ID).
     *
     * @return Een {@link SearchResult} als de film gevonden is, anders null.
     */
    @Override
    public SearchResult findById(String id) {
        logger.debug("Film ophalen met ID: {}", id);
        Movie movie = movieApiStrategy.findById(id);

        return (movie != null) ? toResponse(movie) : null;
    }

    /**
     * Hulpmethode om een Movie entiteit om te zetten naar een MovieResponse DTO.
     * * @param movie Het bron-object (Movie model).
     *
     * @return Een nieuw MovieResponse object gevuld met de filmgegevens.
     */
    private MovieResponse toResponse(Movie movie) {
        return new MovieResponse(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getRated(),
                movie.getReleased(),
                movie.getRuntime(),
                movie.getYear(),
                movie.getDirector(),
                movie.getWriter(),
                movie.getLanguage(),
                movie.getPoster(),
                movie.getActors(),
                movie.getGenres(),
                movie.getImdbRating(),
                movie.getMediaType()
        );
    }
}