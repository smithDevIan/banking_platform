package com.bank.card.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemId {
    private UUID id;
    private String name;

    public ItemId(UUID id){this.id=id;}
    public ItemId(UUID id,String name){
        this.id = id;
        this.name = name;
    }
}
