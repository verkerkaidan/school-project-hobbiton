package com.libraryservice.dao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DAO for Open Library API.
 * <p>
 * Search:  GET <a href="https://openlibrary.org/search.json?q=...&limit=10">...</a>
 * Response: { "docs": [ {...} ] }
 * <p>
 * ISBN:    GET <a href="https://openlibrary.org/api/books?bibkeys=ISBN:xxx&format=json&jscmd=data">...</a>
 * Response: { "ISBN:xxx": { "title": "...", "authors": [...] } }
 * <p>
 * No API key required — Open Library is fully open.
 */
@Component
public class OpenLibraryDAO {

    private static final Logger logger = Logger.getLogger(OpenLibraryDAO.class.getName());
    private static final String BASE_URL_BOOKS = "https://openlibrary.org";

    private final RestTemplate restTemplate;

    /**
     * Constructor die een RestTemplate initialiseert
     * voor HTTP communicatie met de Open Library API.
     */
    public OpenLibraryDAO() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Zoekt boeken via de Open Library API op basis van een zoekterm.
     * <p>
     * Maakt een GET request naar:
     * /search.json?q={query}&limit={maxResults}
     * <p>
     * Waarom RestTemplate.exchange()?
     * - Hiermee kunnen we custom HTTP-headers toevoegen (zoals User-Agent)
     * - Sommige publieke API's (zoals Open Library) kunnen requests blokkeren zonder User-Agent
     * - exchange() geeft meer controle over de volledige HTTP request/response (statuscodes, headers, etc.)
     * <p>
     * In tegenstelling tot getForObject():
     * - getForObject() is simpeler, maar ondersteunt geen headers zonder extra configuratie
     * - Daardoor is exchange() hier betrouwbaarder voor externe API calls
     * <p>
     * De API retourneert een response met een "docs" veld waarin
     * een lijst van boeken zit. Deze methode retourneert die lijst.
     *
     * @param query      de zoekterm (bijv. titel, auteur, keyword)
     * @param maxResults het maximaal aantal resultaten
     * @return een lijst van boeken als Map-structuren, of een lege lijst bij geen resultaat of fout
     */
    public List<Map<String, Object>> searchBooks(String query, int maxResults) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL_BOOKS + "/search.json")
                    .queryParam("q", query)
                    .queryParam("limit", maxResults)
                    .build()
                    .toUri();

            logger.info("Calling OpenLibrary URL: " + uri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warning("OpenLibrary returned status {} for query '{}'");
                return Collections.emptyList();
            }

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("docs")) {
                logger.info("No docs found for query '{}'");
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) body.get("docs");

        } catch (RestClientException e) {
            logger.warning("REST error searching books for query '{}'");
        } catch (Exception e) {
            logger.warning("Unexpected error searching books for query '{}'");
        }

        return Collections.emptyList();
    }

    /**
     * Haalt boekinformatie op via een ISBN-nummer.
     * <p>
     * Maakt een GET request naar:
     * /api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data
     * <p>
     * De API retourneert een JSON-object waarbij de key het ISBN bevat
     * (bijv. "ISBN:9781234567890"). Deze methode retourneert de volledige response map.
     *
     * @param isbn het ISBN-nummer van het boek
     * @return een Map met boekgegevens, of een lege map als niets gevonden is
     * @throws RuntimeException wanneer de API call faalt
     */
    public Map<String, Object> findByIsbn(String isbn) {
        String url = UriComponentsBuilder
                .fromUriString(BASE_URL_BOOKS + "/api/books")
                .queryParam("bibkeys", "ISBN:" + isbn)
                .queryParam("format", "json")
                .queryParam("jscmd", "data")
                .toUriString();

        logger.info("OpenLibraryDAO ISBN lookup: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return (response == null) ? Collections.emptyMap() : response;
        } catch (Exception e) {
            logger.severe("Open Library ISBN error: " + e.getMessage());
            throw new RuntimeException("Open Library ISBN error: " + e.getMessage(), e);
        }
    }
}
