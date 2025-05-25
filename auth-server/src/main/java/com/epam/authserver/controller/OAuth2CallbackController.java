package com.epam.authserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2CallbackController {

    @GetMapping(path = "/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {

        StringBuilder out = new StringBuilder();
        out.append("code=").append(code);
        if (state != null && !state.isBlank()) {
            out.append("\nstate=").append(state);
        }
        return out.toString();
    }
}
