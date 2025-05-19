package com.bank.card.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    private UUID id;
    private String code;
    private String name;

    public Item(UUID id) {
        this.id = id;
    }

    public Item(UUID id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Item(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Item(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
