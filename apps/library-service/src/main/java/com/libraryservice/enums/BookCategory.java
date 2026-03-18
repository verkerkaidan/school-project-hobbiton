package com.libraryservice.enums;

public enum BookCategory {
    FICTION("Fiction"),
    NON_FICTION("Non-Fiction"),
    BIOGRAPHY("Biography"),
    SCIENCE("Science"),
    HISTORY("History"),
    FANTASY("Fantasy"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    THRILLER("Thriller"),
    CHILDREN("Children"),
    YOUNG_ADULT("Young Adult"),
    ALL("All");

    private final String label;

    BookCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
