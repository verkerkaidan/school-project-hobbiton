package com.libraryservice.dto.common;

/**
 * WHY A MARKER INTERFACE FOR RESULTS?
 * <p>
 * SearchableService.search() returns List<SearchResult>.
 * BookResponse and ExerciseResponse both implement this interface.
 * <p>
 * This means the LibraryController can hold and return ANY result type
 * without knowing what it is — Jackson serializes the concrete type
 * (BookResponse or ExerciseResponse) to JSON correctly at runtime.
 * <p>
 * It's a clean way to say "this object is a valid search result"
 * without forcing all domains into the same fields.
 */
public interface SearchResult {
    String getResultType();
}
