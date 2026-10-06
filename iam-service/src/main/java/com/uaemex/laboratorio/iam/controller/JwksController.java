package com.uaemex.laboratorio.iam.controller;

import com.uaemex.laboratorio.iam.token.EmisorTokens;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/.well-known")
public class JwksController {
    private final EmisorTokens emisorTokens;

    public JwksController(EmisorTokens emisorTokens) {
        this.emisorTokens = emisorTokens;
    }

    // Los demas servicios la descargan una vez al arrancar, no en cada peticion
    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {
        return emisorTokens.jwks();
    }
}
