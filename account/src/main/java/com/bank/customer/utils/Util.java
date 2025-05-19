package com.bank.customer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.TimeZone;

public class Util {
    public static String toJson(Object entity) {
        String json = "";
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.setDateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone("EAT")));

        try {
            json = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone("EAT")));

        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Pageable getPageable(int page, int size, String direction, String orderBy) {
        Sort sort;
        if (direction.equals("ASC")) {
            sort = Sort.by(Sort.Direction.ASC, orderBy);
        } else {
            sort = Sort.by(Sort.Direction.DESC, orderBy);
        }
        return PageRequest.of(page, size, sort);
    }

    public static String processSearchName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        String processed = name.replaceAll("[^a-zA-Z]", "").toLowerCase();
        return processed.isEmpty() ? null : processed;
    }

    public static boolean isValid(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return false;
        }

        try {
            Iban.valueOf(iban.replaceAll("\\s+", ""));
            return true;
        } catch (IbanFormatException | InvalidCheckDigitException |
                 UnsupportedCountryException e) {
            return false;
        }
    }

    public static boolean isValidBicSwift(String bic) {
        return bic != null && bic.matches("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    }
}
