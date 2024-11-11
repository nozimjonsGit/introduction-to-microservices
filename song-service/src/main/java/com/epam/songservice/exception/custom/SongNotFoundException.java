package com.epam.songservice.exception.custom;

public class SongNotFoundException extends RuntimeException {
    public SongNotFoundException(String message, Long id) {
        super(String.format(message, id));
    }
}
