package com.epam.resourceprocessor.util;

import com.epam.resourceprocessor.dto.SongMetadataDTO;
import com.epam.resourceprocessor.exception.custom.MetadataExtractionException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@UtilityClass
@Slf4j
public class SongMetadataUtil {
    private static final String TITLE = "dc:title";
    private static final String ARTIST = "xmpDM:artist";
    private static final String ALBUM = "xmpDM:album";
    private static final String DURATION = "xmpDM:duration";
    private static final String RELEASE_DATE = "xmpDM:releaseDate";

    public static SongMetadataDTO extractMetadata(InputStream audioStream) {
        try {
            ContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Mp3Parser mp3Parser = new Mp3Parser();
            ParseContext context = new ParseContext();

            mp3Parser.parse(audioStream, handler, metadata, context);

            String name = metadata.get(TITLE);
            String artist = metadata.get(ARTIST);
            String album = metadata.get(ALBUM);
            String length = formatDuration(metadata.get(DURATION));
            String releaseDate = extractYear(metadata.get(RELEASE_DATE));

            return new SongMetadataDTO(name, artist, album, length, releaseDate);
        } catch (TikaException | IOException | SAXException ex) {
            log.error("Failed to extract metadata from MP3 file", ex);
            throw new MetadataExtractionException("Failed to extract metadata from MP3 file", ex);
        }
    }

    private static String formatDuration(String durationInSeconds) {
        if (durationInSeconds == null) {
            return "";
        }

        try {
            int seconds = (int) Float.parseFloat(durationInSeconds);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private static String extractYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4 ) {
            return "";
        }

        return releaseDate.substring(0, 4);
    }
}
