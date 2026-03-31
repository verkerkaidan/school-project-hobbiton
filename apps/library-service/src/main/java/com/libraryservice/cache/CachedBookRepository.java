package com.libraryservice.cache;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CachedBookRepository extends JpaRepository<BookCache, String> {
    // Spring Data JPA regelt de implementatie hiervan automatisch!
}