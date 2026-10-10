package com.agriculture.farm.controller;

import com.agriculture.farm.dto.Requests.*;
import com.agriculture.farm.dto.Responses.*;
import com.agriculture.farm.entity.*;
import com.agriculture.farm.service.*;
import com.agriculture.farm.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {
    private final FarmService service;

    @PostMapping
    public ResponseEntity<FarmResponse> create(@Valid @RequestBody FarmRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @GetMapping
    public PagedModel<FarmResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return new PagedModel<>(service.list(PageUtil.of(page, size, sortBy, direction)));
    }

    @GetMapping("/{id}")
    public FarmResponse get(@PathVariable Long id) { return service.get(id); }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public FarmResponse update(@PathVariable Long id, @Valid @RequestBody FarmRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
