package com.bank.card.controllers;

import com.bank.card.payloads.CardCreateRequest;
import com.bank.card.services.interfaces.CardService;
import com.bank.card.utils.Constants;
import com.bank.card.utils.Response;
import com.bank.card.utils.ResponseData;
import com.bank.card.utils.Util;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CardCreateRequest cardCreateRequest){
        ResponseData<?> response = cardService.createUpdate(cardCreateRequest,null);
        return getResponseEntity(response);
    }

    private ResponseEntity<?> getResponseEntity(ResponseData<?> response) {
        if (response.getStatus() == Response.ERRORS_OCCURRED.status()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }else if(response.getStatus() == Response.SUCCESS.status()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,@RequestBody @Valid CardCreateRequest cardCreateRequest){
        ResponseData<?> response = cardService.createUpdate(cardCreateRequest,id);
        return getResponseEntity(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(value = "direction", defaultValue = Constants.Pagination.DEFAULT_ORDER_DIRECTION) String direction,
            @RequestParam(value = "orderBy", defaultValue = Constants.Pagination.DEFAULT_ORDER_BY) String orderBy,
            @RequestParam(value = "page", defaultValue = Constants.Pagination.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.Pagination.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "cardType", required = false) String cardType,
            @RequestParam(value = "pan", required = false) String pan,
            @RequestParam(value = "cardAlias", required = false) String cardAlias,
            @RequestParam(value = "unMaskPan", defaultValue = "false") Boolean unMaskPan
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.getAll(Util.getPageable(page, size, direction, orderBy),cardType,pan,cardAlias,unMaskPan));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getOne(@PathVariable UUID id,@RequestParam(value = "unMaskPan", defaultValue = "false") Boolean unMaskPan){
        ResponseData<?> response = cardService.getOne(id,unMaskPan);
        if (response.getStatus() == Response.CARD_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        ResponseData<?> response = cardService.delete(id);
        if (response.getStatus() == Response.CARD_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
