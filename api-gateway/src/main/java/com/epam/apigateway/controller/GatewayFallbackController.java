package com.epam.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @GetMapping("/{route}")
    public Mono<ResponseEntity<Map<String, Object>>> fallbackForRoute(
            @PathVariable String route,
            ServerHttpRequest request
    ) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                "path", request.getPath().value(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "message", String.format("%s service is currently unavailable",
                        route.substring(0, 1).toUpperCase() + route.substring(1))
        );

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
        );
    }
}
