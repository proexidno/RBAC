package com.example.taxi.notification.controller;

import com.example.taxi.notification.dto.NotificationCreateRequest;
import com.example.taxi.notification.model.NotificationTask;
import com.example.taxi.notification.service.NotificationQueueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    private final NotificationQueueService queueService;

    public NotificationController(NotificationQueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/notifications")
    NotificationTask create(@Valid @RequestBody NotificationCreateRequest request) {
        return queueService.create(request);
    }

    @GetMapping(value = "/notifications", params = "trip_id")
    List<NotificationTask> byTrip(@RequestParam("trip_id") Long tripId) {
        return queueService.byTrip(tripId);
    }
}

