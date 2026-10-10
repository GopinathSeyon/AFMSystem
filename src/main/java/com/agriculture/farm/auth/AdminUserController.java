package com.agriculture.farm.auth;

import com.agriculture.farm.dto.Requests.*;
import com.agriculture.farm.dto.Responses.UserSummary;
import com.agriculture.farm.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService service;

    @GetMapping
    public PagedModel<UserSummary> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(defaultValue = "id") String sortBy,
                                        @RequestParam(defaultValue = "asc") String direction) {
        return new PagedModel<>(service.list(PageUtil.of(page, size, sortBy, direction)));
    }

    @GetMapping("/{id}")
    public UserSummary get(@PathVariable Long id) { return service.get(id); }

    @PatchMapping("/{id}/role")
    public UserSummary role(@PathVariable Long id, @Valid @RequestBody RoleRequest r) {
        return service.changeRole(id, r.role());
    }

    @PatchMapping("/{id}/active")
    public UserSummary active(@PathVariable Long id, @Valid @RequestBody ActiveRequest r) {
        return service.setActive(id, r.active());
    }

    @PutMapping("/{id}/farmer")
    public UserSummary link(@PathVariable Long id, @Valid @RequestBody LinkFarmerRequest r) {
        return service.linkFarmer(id, r.farmerId());
    }
}
