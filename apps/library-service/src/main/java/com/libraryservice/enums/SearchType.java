package com.libraryservice.enums;

/**
 * The SearchServiceFactory uses this enum to decide which
 * service to return. If someone sends "type": "BOOK", the factory
 * returns BookService. "GYM" returns GymService.
 * <p>
 * Adding a new domain (e.g. MOVIE) = add one line here +
 * one new service. The controller and factory need minimal changes.
 */
public enum SearchType {
    BOOK,
    EXERCISE,
    MOVIE
}
