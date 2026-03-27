package com.libraryservice.model;

public class Movie {
    private String movieId;
    private String title;
    private String description;
    private String rated;
    private String released;
    private String runtime;
    private String year;
    private String director;
    private String writer;
    private String language;
    private String poster;
    private String actors;
    private String genres;
    private String imdbRating;
    private String mediaType;

    public Movie() {
    }

    public Movie(String movieId, String title, String description, String rated, String released, String runtime, String year, String director, String writer, String language, String poster, String actors, String genres, String imdbRating, String mediaType) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.rated = rated;
        this.released = released;
        this.runtime = runtime;
        this.year = year;
        this.director = director;
        this.writer = writer;
        this.language = language;
        this.poster = poster;
        this.actors = actors;
        this.genres = genres;
        this.imdbRating = imdbRating;
        this.mediaType = mediaType;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRated() {
        return rated;
    }

    public String getReleased() {
        return released;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getWriter() {
        return writer;
    }

    public String getLanguage() {
        return language;
    }

    public String getPoster() {
        return poster;
    }

    public String getActors() {
        return actors;
    }

    public String getGenres() {
        return genres;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public String getMediaType() {
        return mediaType;
    }
}


