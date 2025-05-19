package com.bank.card.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemCode {
    private String code;
    private String name;

    public ItemCode(String code){this.code=code;}

    public ItemCode(String code,String name){
        this.code=code;
        this.name=name;
    }
}
