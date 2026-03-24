package com.libraryservice.dto.common;

/**
 * WAAROM EEN MARKER INTERFACE VOOR RESULTATEN?
 * <p>
 * SearchableService.search() retourneert een List<SearchResult>.
 * BookResponse en ExerciseResponse implementeren beide deze interface.
 * <p>
 * Dit betekent dat de LibraryController elk type resultaat kan verwerken
 * en teruggeven zonder te weten welk specifiek type het is.
 * Jackson serialiseert automatisch het concrete type
 * (BookResponse of ExerciseResponse) naar JSON tijdens runtime.
 * <p>
 * Dit is een nette manier om aan te geven:
 * "dit object is een geldig zoekresultaat"
 * zonder dat alle domeinen dezelfde velden moeten hebben.
 */
public interface SearchResult {
    String getResultType();
}
