package com.epam.resourceservice.util.validator;

import com.epam.resourceservice.exception.custom.InvalidInputException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CustomValidator {

    private static final int MAX_CSV_LENGTH = 200;

    public static List<Long> validateAndParseCsv(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            throw new InvalidInputException("CSV string is empty.");
        }

        if (csvString.length() > MAX_CSV_LENGTH) {
            throw new InvalidInputException(String.format("CSV string is too long: received %d characters, maximum allowed length is %d characters",
                    csvString.length(), MAX_CSV_LENGTH));
        }

        List<Long> ids = new ArrayList<>();
        for (String idStr : csvString.split(",")) {
            String trimmedId = idStr.trim();
            if (trimmedId.isEmpty()) {
                throw new InvalidInputException("CSV string contains empty values.");
            }
            try {
                long id = Long.parseLong(trimmedId);
                if (id <= 0) {
                    throw new InvalidInputException("CSV string contains non-positive integers. IDs must be positive.");
                }
                ids.add(id);
            } catch (NumberFormatException e) {
                throw new InvalidInputException("CSV string contains invalid number format: '" + trimmedId + "'. Expected a positive integer.");
            }
        }

        return ids;
    }

    public static void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("ID cannot be null or empty.");
        }

        try {
            long idValue = Long.parseLong(id);
            if (idValue <= 0) {
                throw new InvalidInputException("ID must be a positive number.");
            }
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid value : '" + id + "' for ID. Expected a positive integer.");
        }
    }
}