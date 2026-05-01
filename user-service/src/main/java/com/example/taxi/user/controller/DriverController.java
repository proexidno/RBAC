package com.example.taxi.user.controller;

import com.example.taxi.user.dto.DriverCreateRequest;
import com.example.taxi.user.dto.DriverStatusRequest;
import com.example.taxi.user.model.Driver;
import com.example.taxi.user.service.DriverService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DriverController {
    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/drivers")
    Driver create(@Valid @RequestBody DriverCreateRequest request) {
        return driverService.create(request);
    }

    @GetMapping("/drivers/{id}")
    Driver get(@PathVariable Long id) {
        return driverService.get(id);
    }

    @GetMapping("/drivers/available")
    List<Driver> available() {
        return driverService.availableDrivers();
    }

    @PostMapping("/drivers/reserve")
    Driver reserve() {
        return driverService.reserveAvailableDriver()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No available drivers"));
    }

    @PatchMapping("/drivers/{id}/status")
    Driver updateStatus(@PathVariable Long id, @Valid @RequestBody DriverStatusRequest request) {
        return driverService.updateStatus(id, request.status());
    }
}
