package com.housemate.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@Profile("staging")
public class StagingManifestController {

    private static final Logger log = LoggerFactory.getLogger(StagingManifestController.class);

    private static final String FALLBACK_MANIFEST = "{\n"
            + "  \"name\": \"HouseMate - Staging\",\n"
            + "  \"short_name\": \"HM Staging\",\n"
            + "  \"start_url\": \".\",\n"
            + "  \"display\": \"standalone\",\n"
            + "  \"background_color\": \"#0175C2\",\n"
            + "  \"theme_color\": \"#0175C2\",\n"
            + "  \"description\": \"HouseMate App (Staging)\",\n"
            + "  \"orientation\": \"portrait-primary\",\n"
            + "  \"prefer_related_applications\": false\n"
            + "}";

    @Value("classpath:static/manifest.json")
    private Resource manifest;

    @GetMapping(value = "/manifest.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getManifest() {
        try {
            if (manifest != null && manifest.exists()) {
                try (InputStream inputStream = manifest.getInputStream()) {
                    String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    return content
                            .replace("\"name\": \"HouseMate\"", "\"name\": \"HouseMate - Staging\"")
                            .replace("\"short_name\": \"HouseMate\"", "\"short_name\": \"HM Staging\"");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read static manifest resource, returning fallback: {}", e.getMessage());
        }
        return FALLBACK_MANIFEST;
    }
}
