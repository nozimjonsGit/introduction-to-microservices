package com.epam.resourceservice.util.validator;

import com.epam.resourceservice.exception.custom.InvalidInputException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CsvValidator {

    private static final int MAX_CSV_LENGTH = 200;

    public static List<Long> validateAndParseCsv(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            throw new InvalidInputException("CSV string is empty.");
        }

        if (csvString.length() > MAX_CSV_LENGTH) {
            throw new InvalidInputException("CSV string length exceeds the maximum allowed length of " + MAX_CSV_LENGTH);
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
}
