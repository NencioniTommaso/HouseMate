package com.housemate.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StagingManifestController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("staging")
@DisplayName("StagingManifestController Unit Tests")
class StagingManifestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return modified manifest with staging name and short_name")
    void shouldReturnModifiedManifest() throws Exception {
        mockMvc.perform(get("/manifest.json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("HouseMate - Staging"))
                .andExpect(jsonPath("$.short_name").value("HM Staging"));
    }
}
